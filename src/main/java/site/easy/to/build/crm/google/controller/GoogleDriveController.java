package site.easy.to.build.crm.google.controller;

import com.google.api.client.http.HttpResponseException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import site.easy.to.build.crm.entity.OAuthUser;
import site.easy.to.build.crm.google.model.drive.GoogleDriveFile;
import site.easy.to.build.crm.google.model.drive.GoogleDriveFolder;
import site.easy.to.build.crm.google.service.drive.GoogleDriveApiService;
import site.easy.to.build.crm.util.AuthenticationUtils;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@Controller
@RequestMapping("/employee/drive")
public class GoogleDriveController {

    private final GoogleDriveApiService googleDriveApiService;

    private final AuthenticationUtils authenticationUtils;

    @Autowired
    public GoogleDriveController(GoogleDriveApiService googleDriveApiService, AuthenticationUtils authenticationUtils) {
        this.googleDriveApiService = googleDriveApiService;
        this.authenticationUtils = authenticationUtils;
    }

    @GetMapping("/list-files")
    public String listFilesWithFolder(Model model, Authentication authentication) {
        if((authentication instanceof UsernamePasswordAuthenticationToken)) {
            return "/google-error";
        }
        OAuthUser oAuthUser = authenticationUtils.getOAuthUserFromAuthentication(authentication);
        List<GoogleDriveFile> files;
        List<GoogleDriveFolder> folders;
        try {
            files = googleDriveApiService.listFiles(oAuthUser);
            folders = googleDriveApiService.listFolders(oAuthUser);
        } catch (IOException | GeneralSecurityException e) {
            return handleGoogleDriveApiException(model,e);
        }

        model.addAttribute("files", files);
        model.addAttribute("folders", folders);
        return "google-drive/list-files";
    }
    @GetMapping("/folder/{id}")
    public String listFilesInFolder(Model model, @ModelAttribute("file") GoogleDriveFile file, BindingResult bindingResult, Authentication authentication, @PathVariable("id") String id,
                                     RedirectAttributes redirectAttributes){
        if((authentication instanceof UsernamePasswordAuthenticationToken)) {
            return "/google-error";
        }
        OAuthUser oAuthUser = authenticationUtils.getOAuthUserFromAuthentication(authentication);

        List<GoogleDriveFile> files;
        try {
            files = googleDriveApiService.listFilesInFolder(oAuthUser,id);
        } catch (IOException | GeneralSecurityException e) {
            bindingResult.rejectValue("failedErrorMessage", "error.failedErrorMessage","There are might be a problem retrieving the file information, please try again later!");
            redirectAttributes.addFlashAttribute("bindingResult", bindingResult);
            return "redirect:/employee/drive/list-files";
        }
        model.addAttribute("files", files);
        return "google-drive/list-files-in-folder";
    }

    @GetMapping("/create-folder")
    public String showFolderCreationForm(Model model, Authentication authentication){
        if((authentication instanceof UsernamePasswordAuthenticationToken)) {
            return "/google-error";
        }
        OAuthUser oAuthUser = authenticationUtils.getOAuthUserFromAuthentication(authentication);
        if(!oAuthUser.getGrantedScopes().contains("https://www.googleapis.com/auth/drive.file")) {
            String code = "403";
            String link = "employee/settings/google-services";
            String buttonText = "Grant Access";
            String message = "Please grant the app access to Google Drive, in order to use this service";
            model.addAttribute("link",link);
            model.addAttribute("message",message);
            model.addAttribute("buttonText",buttonText);
            model.addAttribute("code",code);
            return "gmail/error";
        }
        model.addAttribute("folder",new GoogleDriveFolder());
        return "google-drive/create-folder";
    }
    @PostMapping("/create-folder")
    public String createFolder(Authentication authentication, @ModelAttribute("folder") @Valid GoogleDriveFolder folder,
                               BindingResult bindingResult, Model model) {
        if((authentication instanceof UsernamePasswordAuthenticationToken)) {
            return "/google-error";
        }
        if (bindingResult.hasErrors()) {
            return "google-drive/create-folder";
        }
        OAuthUser oAuthUser = authenticationUtils.getOAuthUserFromAuthentication(authentication);
        try {
            googleDriveApiService.createFolder(oAuthUser, folder.getName());
        } catch (GeneralSecurityException | IOException e) {
            return handleGoogleDriveApiException(model,e);
        }
        return "redirect:/employee/drive/list-files";
    }
    @GetMapping("/create-file")
    public String showFileCreationForm(Model model, Authentication authentication){
        if((authentication instanceof UsernamePasswordAuthenticationToken)) {
            return "/google-error";
        }

        List<GoogleDriveFolder> folders;
        OAuthUser oAuthUser = authenticationUtils.getOAuthUserFromAuthentication(authentication);
        try {
            folders = googleDriveApiService.listFolders(oAuthUser);
        } catch (GeneralSecurityException | IOException e) {
            return handleGoogleDriveApiException(model,e);
        }
        model.addAttribute("folders",folders);
        model.addAttribute("file",new GoogleDriveFile());
        return "google-drive/create-file";
    }
    @PostMapping("/create-file")
    public String createFileInFolder(Authentication authentication, @ModelAttribute("file") @Valid GoogleDriveFile file,
                                     BindingResult bindingResult, Model model) {
        if((authentication instanceof UsernamePasswordAuthenticationToken)) {
            return "/google-error";
        }
        List<GoogleDriveFolder> folders;
        OAuthUser oAuthUser = authenticationUtils.getOAuthUserFromAuthentication(authentication);
        try {
            folders = googleDriveApiService.listFolders(oAuthUser);
        } catch (IOException | GeneralSecurityException e) {
            return handleGoogleDriveApiException(model,e);
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("folders",folders);
            return "google-drive/create-file";
        }

        try {
            googleDriveApiService.createFileInFolder(oAuthUser, file.getName(), file.getFolderId(), file.getMimeType());
        } catch (GeneralSecurityException | IOException e) {
            return handleGoogleDriveApiException(model,e);
        }
        return "redirect:/employee/drive/list-files";
    }

    @PostMapping("/ajax-share")
    @ResponseBody
    public ResponseEntity<String> shareFileWithUsers(Authentication authentication, @RequestParam("id") String id,
                                                     @RequestParam("emails") String emails, @RequestParam("role") String role) {
        if (authentication instanceof UsernamePasswordAuthenticationToken) {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Location", "/google-error");
            return ResponseEntity.status(HttpStatus.FOUND).headers(headers).build();
        }
        OAuthUser oAuthUser = authenticationUtils.getOAuthUserFromAuthentication(authentication);
        if(emails == null || emails.isEmpty()){
            return ResponseEntity.badRequest().body("Email is required");
        }
        List<String> users = List.of(emails.split(","));
        try {
            for(String user : users) {
                googleDriveApiService.shareFileWithUser(oAuthUser, id, user, role);
            }
        } catch (IOException | GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.ok("Success");
    }
    @PostMapping("/ajax-delete")
    @ResponseBody
    public ResponseEntity<String> deleteFile(Authentication authentication, @RequestParam("id") String id) {
        if (authentication instanceof UsernamePasswordAuthenticationToken) {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Location", "/google-error");
            return ResponseEntity.status(HttpStatus.FOUND).headers(headers).build();
        }
        OAuthUser oAuthUser = authenticationUtils.getOAuthUserFromAuthentication(authentication);
        try {
            googleDriveApiService.deleteFile(oAuthUser, id);
        } catch (IOException | GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
        return ResponseEntity.ok("Success");
    }

    private String handleGoogleDriveApiException(Model model, Exception e){
        String link = "";
        String buttonText = "Go Home";
        String message = "There was a problem with Google Drive, Please try again later!";
        String code = "400";
        if (e instanceof HttpResponseException httpResponseException) {
            int statusCode = httpResponseException.getStatusCode();
            if(statusCode == 403){
                code = "403";
                link = "employee/settings/google-services";
                buttonText = "Grant Access";
                message = "Please grant the app access to Google Drive, in order to use this service";
            }
        }

        model.addAttribute("link",link);
        model.addAttribute("message",message);
        model.addAttribute("buttonText",buttonText);
        model.addAttribute("code",code);
        return "gmail/error";
    }

}

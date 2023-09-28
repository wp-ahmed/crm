package site.easy.to.build.crm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPattern;
import site.easy.to.build.crm.entity.User;
import site.easy.to.build.crm.service.user.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
@RequestMapping("/manager")
public class UrlAccessController {

    private final RequestMappingHandlerMapping handlerMapping;
    private final UserService userService;

    @Autowired
    public UrlAccessController(RequestMappingHandlerMapping handlerMapping, UserService userService) {
        this.handlerMapping = handlerMapping;
        this.userService = userService;
    }

    @GetMapping("/urls")
    public String showAvailableUrls(Model model) {
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = handlerMapping.getHandlerMethods();
        List<String> urls = new ArrayList<>();
        List<User> users = userService.findAll();
        for (RequestMappingInfo info : handlerMethods.keySet()) {
            if (info.getPathPatternsCondition() != null) {
                Set<PathPattern> patterns = info.getPathPatternsCondition().getPatterns();
                if (!patterns.isEmpty()) {
                    PathPattern pattern = patterns.iterator().next();
                    String link = pattern.getPatternString();
                    if (info.getMethodsCondition().getMethods().contains(RequestMethod.GET)) {
                        // Define the regular expression pattern
                        Pattern patternToRemove = Pattern.compile("\\{.*?\\}");

                        // Use a Matcher to find and remove all occurrences of the pattern
                        Matcher matcher = patternToRemove.matcher(link);
                        String modifiedUrl = matcher.replaceAll("").replaceFirst("/","");
                        if(!modifiedUrl.isEmpty()) {
                            urls.add(modifiedUrl);
                        }
                    }
                }
            }
        }
        model.addAttribute("users",users);
        model.addAttribute("urls", urls);
        return "url/all";
    }
}
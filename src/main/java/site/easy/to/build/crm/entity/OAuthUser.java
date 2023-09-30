package site.easy.to.build.crm.entity;

import jakarta.persistence.*;
import site.easy.to.build.crm.converter.StringSetConverter;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "oauth_users")
public class OAuthUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "granted_scopes")
    @Convert(converter = StringSetConverter.class)
    private Set<String> grantedScopes = new HashSet<>();

    @Column(name = "access_token")
    private String accessToken;

    @Column(name = "access_token_issued_at")
    private Instant accessTokenIssuedAt;

    @Column(name = "access_token_expiration")
    private Instant accessTokenExpiration;

    @Column(name = "refresh_token")
    private String refreshToken;

    @Column(name = "refresh_token_issued_at")
    private Instant refreshTokenIssuedAt;

    @Column(name = "refresh_token_expiration")
    private Instant refreshTokenExpiration;

    @Column(name = "email")
    private String email;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    public OAuthUser() {
    }

    public OAuthUser(Set<String> grantedScopes, String accessToken, Instant accessTokenIssuedAt, Instant accessTokenExpiration, String refreshToken,
                     Instant refreshTokenIssuedAt, Instant refreshTokenExpiration, User user, String  email) {
        this.grantedScopes = grantedScopes;
        this.accessToken = accessToken;
        this.accessTokenIssuedAt = accessTokenIssuedAt;
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshToken = refreshToken;
        this.refreshTokenIssuedAt = refreshTokenIssuedAt;
        this.refreshTokenExpiration = refreshTokenExpiration;
        this.user = user;
        this.email = email;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Set<String> getGrantedScopes() {
        return grantedScopes;
    }

    public void setGrantedScopes(Set<String> grantedScopes) {
        this.grantedScopes = grantedScopes;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public Instant getAccessTokenIssuedAt() {
        return accessTokenIssuedAt;
    }

    public void setAccessTokenIssuedAt(Instant accessTokenIssuedAt) {
        this.accessTokenIssuedAt = accessTokenIssuedAt;
    }

    public Instant getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    public void setAccessTokenExpiration(Instant accessTokenExpiration) {
        this.accessTokenExpiration = accessTokenExpiration;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Instant getRefreshTokenIssuedAt() {
        return refreshTokenIssuedAt;
    }

    public void setRefreshTokenIssuedAt(Instant refreshTokenIssuedAt) {
        this.refreshTokenIssuedAt = refreshTokenIssuedAt;
    }

    public Instant getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }

    public void setRefreshTokenExpiration(Instant refreshTokenExpiration) {
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}

package  render.casino.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.codec.binary.Base32;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import render.casino.util.GeoUtil;
import render.casino.util.MyDecimal;
import render.casino.util.StringUtil;

import java.security.SecureRandom;
import java.util.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotBlank
    @Size(min = 5, max = 32)
    @Column(unique = true)
    private String username;

    @NotBlank
    @Email
    @Column(unique = true)
    private String email;

    @Size(min = 2, max = 32)
    @Column(unique = true)
    private String firstName;

    @Size(min = 2, max = 32)
    @Column(unique = true)
    private String lastName;

    @Column(unique = true)
    private String phone;

    @NotBlank
    @Size(min = 8, max = 64)
    private String password;

    private String promocodeName;

    @Column(columnDefinition = "VARCHAR(128) DEFAULT ''")
    private String note;

    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(pattern = "dd/MM/yyyy HH:mm:ss")
    private Date registered;

    @Size(max = 64)
    private String domain;

    @Column(columnDefinition = "VARCHAR(128) DEFAULT ''")
    private String referrer;

    private boolean twoFactorEnabled;

    @NotBlank
    @Size(max = 32)
    private String twoFactorCode;

    @NotBlank
    @Size(max = 128)
    private String regIp;

    @Column(columnDefinition = "VARCHAR(128) DEFAULT 'N/A'")
    @Size(max = 128)
    private String platform;

    @Column(columnDefinition = "VARCHAR(8) DEFAULT 'NO'")
    private String regCountryCode;

    @Column(columnDefinition = "VARCHAR(8) DEFAULT 'NO'")
    private String lastCountryCode;

    @Size(max = 64)
    private String lastIp;

    private long lastActivity;

    private long lastOnline;

    private boolean firstDepositBonusEnabled;

    private double firstDepositBonusAmount;

    private int authCount;

    private double verifDepositAmount;

    @Column(columnDefinition = "DOUBLE DEFAULT 0")
    private double btcVerifDepositAmount;

    @Column(columnDefinition = "DOUBLE DEFAULT -1")
    private double depositCommission;

    @Column(columnDefinition = "DOUBLE DEFAULT -1")
    private double withdrawCommission;

    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean verificationModal;

    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean amlModal;

    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean emailConfirmed;

    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean fakeVerified;

    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean kyc3sended;

    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean emailEnd;

    private long depositsCount;

    @Column(columnDefinition = "INT DEFAULT 0")
    private int smartDepositStep;

    private double deposits;

    @Column(columnDefinition = "TINYINT DEFAULT 0")
    private int roleType;

    @ManyToOne(cascade={CascadeType.ALL})
    @JoinColumn(name="support_id")
    private User support;

    @OneToMany(mappedBy="support")
    private Set<User> supported = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<UserRole> userRoles = new HashSet<>();

    //todo: first deposit bonus amount/enabled
    public User(String username, String email, String password, String promocodeName, String domain, String regIp, String platform, String countryCode, boolean firstDepositBonusEnabled, double firstDepositBonusAmount, boolean emailConfirmed, boolean invUser) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.promocodeName = promocodeName;
        this.regIp = regIp;
        this.lastIp = regIp;
        this.lastActivity = System.currentTimeMillis();
        this.lastOnline = this.lastActivity;
        this.domain = domain;
        this.platform = platform;
        this.regCountryCode = countryCode;
        this.lastCountryCode = countryCode;
        this.registered = new Date();
        this.twoFactorEnabled = false;
        this.twoFactorCode = generateTwoFactorCode();
        this.firstDepositBonusEnabled = firstDepositBonusEnabled;
        this.firstDepositBonusAmount = firstDepositBonusAmount;
        this.emailConfirmed = emailConfirmed;
        this.roleType = UserRoleType.ROLE_USER.ordinal();
    }

    public User(String username, String firstName, String lastName, String email, String phone, String password, String promocodeName, String domain, String regIp, String platform, String countryCode, boolean firstDepositBonusEnabled, double firstDepositBonusAmount, boolean emailConfirmed, boolean invUser) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.promocodeName = promocodeName;
        this.regIp = regIp;
        this.lastIp = regIp;
        this.lastActivity = System.currentTimeMillis();
        this.lastOnline = this.lastActivity;
        this.domain = domain;
        this.platform = platform;
        this.regCountryCode = countryCode;
        this.lastCountryCode = countryCode;
        this.registered = new Date();
        this.twoFactorEnabled = false;
        this.twoFactorCode = generateTwoFactorCode();
        this.firstDepositBonusEnabled = firstDepositBonusEnabled;
        this.firstDepositBonusAmount = firstDepositBonusAmount;
        this.emailConfirmed = emailConfirmed;
        this.roleType = UserRoleType.ROLE_USER.ordinal();
    }

    public void addToSupported(User userToAdd) {
        supported.add(userToAdd);
        userToAdd.setSupport(this);
    }

    public void removeFromSupported(User userToRemove) {
        supported.remove(userToRemove);
        userToRemove.setSupport(null);
    }

    @Transient
    public boolean isAdmin() {
        return this.userRoles.stream().anyMatch(userRole -> userRole.getName() == UserRoleType.ROLE_ADMIN);
    }

    @Transient
    public boolean isWorker() {
        return this.userRoles.stream().anyMatch(userRole -> userRole.getName() == UserRoleType.ROLE_WORKER);
    }

    @Transient
    public boolean isSupporter() {
        return this.userRoles.stream().anyMatch(userRole -> userRole.getName() == UserRoleType.ROLE_SUPPORTER);
    }

    @Transient
    public boolean isStaff() {
        return isAdmin() || isWorker() || isSupporter();
    }

    @Transient
    public String getFormattedLastActivity() {
        long diff = (System.currentTimeMillis() - this.lastActivity) / 1000L;
        if (diff < 60) {
            return diff + " сек. назад";
        } else if (diff > 86400) {
            return StringUtil.formatDate(new Date(this.lastActivity));
        } else if (diff > 3600) {
            return diff / 3600 + "ч. назад";
        } else {
            return diff / 60 + " мин. назад";
        }
    }

    @Transient
    public String getFormattedRegistered() {
        return StringUtil.formatDate(this.registered);
    }

    @Transient
    public String formattedLastActivityEng() {
        return StringUtil.formatDate(new Date(this.lastActivity));
    }

    @Transient
    public GeoUtil.GeoData getGeolocation() {
        return GeoUtil.getGeo(this.lastIp);
    }

    @Transient
    public boolean isOnline() {
        return this.lastOnline >= System.currentTimeMillis() - (10 * 1000);
    }

    @Transient
    public MyDecimal formattedDeposits() {
        return new MyDecimal(this.deposits, true);
    }

    @Transient
    public long getFakeId() {
        long id = this.id + 1127923;
        String idString = String.valueOf(id);
        double multiplier = Double.parseDouble(idString.substring(idString.length() - 3)) / 100D;
        return (long) (id * multiplier);
    }

    private static String generateTwoFactorCode() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[20];
        random.nextBytes(bytes);
        Base32 base32 = new Base32();
        return base32.encodeToString(bytes);
    }
}

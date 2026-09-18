package lms.ui;

import lms.model.Member;
import lms.model.Person;
import lms.service.DashboardService;
import lms.service.LibraryService;
import lms.util.Config;

import javax.swing.*;
import java.awt.*;

/**
 * Main application window after sign-in. Owns the sidebar navigation and a
 * single swappable content area; every page is a JPanel built around the
 * existing service layer. Page selection is restricted by role.
 */
public class DashboardFrame extends JFrame {

    private final Person user;
    private final Config config;
    private final LibraryService service;
    private final DashboardService dashboardService;

    private final SidebarPanel sidebar;
    private final HeaderPanel header;
    private final JPanel contentPanel;

    public DashboardFrame(Person user, Config config, LibraryService service, DashboardService dashboardService) {
        this.user = user;
        this.config = config;
        this.service = service;
        this.dashboardService = dashboardService;

        setTitle("Library Management System");
        setSize(1280, 800);
        setMinimumSize(new Dimension(1080, 680));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(UITheme.BACKGROUND);

        sidebar = new SidebarPanel(user.getRole(), this::navigate, this::signOut);
        header = new HeaderPanel(user.getName(), user.getRole());

        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(true);
        contentPanel.setBackground(UITheme.BACKGROUND);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 28, 24, 28));

        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(UITheme.BACKGROUND);
        main.add(header, BorderLayout.NORTH);
        main.add(contentPanel, BorderLayout.CENTER);

        add(sidebar, BorderLayout.WEST);
        add(main, BorderLayout.CENTER);

        navigate("dashboard");
    }

    /** Routes a sidebar key to the page for the signed-in role. */
    private void navigate(String key) {
        contentPanel.removeAll();
        boolean isMember = "MEMBER".equals(user.getRole());
        header.setPageTitle(switch (key) {
            case "books" -> isMember ? "Catalog" : "Books";
            case "members" -> "Members";
            case "circulation" -> "Circulation";
            case "holds", "myholds" -> "Holds";
            case "fines", "myfines" -> "Fines";
            case "myloans" -> "My Loans";
            case "reports" -> "Reports";
            case "settings" -> "Settings";
            default -> "Dashboard";
        });

        JPanel page = switch (key) {
            case "books" -> new BooksPanel(config, service, user);
            case "members" -> new MembersPanel(config, service, user);
            case "circulation" -> new CirculationPanel(config, service, user);
            case "holds" -> new HoldsPanel(config, service, user, false);
            case "fines" -> new FinesPanel(config, service, user, false);
            case "myloans" -> new MemberLoansPanel(config, service, user);
            case "myholds" -> new HoldsPanel(config, service, user, true);
            case "myfines" -> new FinesPanel(config, service, user, true);
            case "reports" -> new ReportsPanel(config, service);
            case "settings" -> new SettingsPanel(config, service, user);
            default -> isMember
                    ? new MemberDashboardPanel(config, dashboardService, service, (Member) user)
                    : new DashboardPanel(dashboardService, user.getRole());
        };
        contentPanel.add(page, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void signOut() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Sign out of the library system?", "Sign Out",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            new LoginFrame(config, service, dashboardService).setVisible(true);
        }
    }
}

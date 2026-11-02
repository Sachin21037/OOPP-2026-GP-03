package service;

import dao.AdminDashboardDAO;
import model.AdminDashboardStats;
import model.RecentActivityItem;

import java.util.List;

/**
 * Service layer coordinating administrative dashboard operations and business logic.
 */
public class AdminDashboardService {

    private static AdminDashboardService instance;
    private final AdminDashboardDAO dashboardDAO;

    public AdminDashboardService() {
        this.dashboardDAO = new AdminDashboardDAO();
    }

    public static synchronized AdminDashboardService getInstance() {
        if (instance == null) {
            instance = new AdminDashboardService();
        }
        return instance;
    }

    /**
     * Loads aggregated dashboard statistical metrics.
     *
     * @return AdminDashboardStats
     */
    public AdminDashboardStats getDashboardStats() {
        return dashboardDAO.getDashboardStatistics();
    }

    /**
     * Loads recent activity items (notices, announcements, and recent activities).
     * If notices exist, returns recent notices; otherwise falls back to recent registrations.
     *
     * @param limit maximum items to retrieve
     * @return List of RecentActivityItem
     */
    public List<RecentActivityItem> getRecentActivities(int limit) {
        List<RecentActivityItem> notices = dashboardDAO.getRecentNotices(limit);
        if (notices != null && !notices.isEmpty()) {
            return notices;
        }
        return dashboardDAO.getRecentUserRegistrations(limit);
    }
}

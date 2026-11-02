package model;

/**
 * Data model representing aggregated statistical metrics for the FAMS Admin Dashboard.
 */
public class AdminDashboardStats {
    private int totalStudents;
    private int totalLecturers;
    private int totalTechnicalOfficers;
    private int totalCourses;
    private int totalNotices;
    private boolean dbConnected;
    private String databaseVersion;
    private String activePort;

    public AdminDashboardStats() {
        this.totalStudents = 0;
        this.totalLecturers = 0;
        this.totalTechnicalOfficers = 0;
        this.totalCourses = 0;
        this.totalNotices = 0;
        this.dbConnected = false;
        this.databaseVersion = "Unknown";
        this.activePort = "3307";
    }

    public AdminDashboardStats(int totalStudents, int totalLecturers, int totalTechnicalOfficers,
                               int totalCourses, int totalNotices, boolean dbConnected,
                               String databaseVersion, String activePort) {
        this.totalStudents = totalStudents;
        this.totalLecturers = totalLecturers;
        this.totalTechnicalOfficers = totalTechnicalOfficers;
        this.totalCourses = totalCourses;
        this.totalNotices = totalNotices;
        this.dbConnected = dbConnected;
        this.databaseVersion = databaseVersion;
        this.activePort = activePort;
    }

    public int getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(int totalStudents) {
        this.totalStudents = totalStudents;
    }

    public int getTotalLecturers() {
        return totalLecturers;
    }

    public void setTotalLecturers(int totalLecturers) {
        this.totalLecturers = totalLecturers;
    }

    public int getTotalTechnicalOfficers() {
        return totalTechnicalOfficers;
    }

    public void setTotalTechnicalOfficers(int totalTechnicalOfficers) {
        this.totalTechnicalOfficers = totalTechnicalOfficers;
    }

    public int getTotalCourses() {
        return totalCourses;
    }

    public void setTotalCourses(int totalCourses) {
        this.totalCourses = totalCourses;
    }

    public int getTotalNotices() {
        return totalNotices;
    }

    public void setTotalNotices(int totalNotices) {
        this.totalNotices = totalNotices;
    }

    public boolean isDbConnected() {
        return dbConnected;
    }

    public void setDbConnected(boolean dbConnected) {
        this.dbConnected = dbConnected;
    }

    public String getDatabaseVersion() {
        return databaseVersion;
    }

    public void setDatabaseVersion(String databaseVersion) {
        this.databaseVersion = databaseVersion;
    }

    public String getActivePort() {
        return activePort;
    }

    public void setActivePort(String activePort) {
        this.activePort = activePort;
    }

    @Override
    public String toString() {
        return "AdminDashboardStats{" +
                "students=" + totalStudents +
                ", lecturers=" + totalLecturers +
                ", officers=" + totalTechnicalOfficers +
                ", courses=" + totalCourses +
                ", notices=" + totalNotices +
                ", dbConnected=" + dbConnected +
                ", dbVersion='" + databaseVersion + '\'' +
                ", port='" + activePort + '\'' +
                '}';
    }
}

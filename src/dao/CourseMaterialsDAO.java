package dao;

import database.DBConnection;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * CourseMaterialsDAO - Data Access Object for Course Materials.
 * Handles database CRUD operations, file storage in materials/ folder, queries, and metrics.
 */
public class CourseMaterialsDAO {

    public static class CourseOfferingItem {
        public final String offeringId;
        public final String courseCode;
        public final String courseName;
        public final String academicYear;
        public final String semester;

        public CourseOfferingItem(String offeringId, String courseCode, String courseName, String academicYear, String semester) {
            this.offeringId = offeringId != null ? offeringId : "";
            this.courseCode = courseCode != null ? courseCode : "";
            this.courseName = courseName != null ? courseName : "";
            this.academicYear = academicYear != null ? academicYear : "";
            this.semester = semester != null ? semester : "";
        }

        @Override
        public String toString() {
            return courseCode + " - " + courseName;
        }
    }

    public static class MaterialRecord {
        public final String materialId;
        public final String offeringId;
        public final String courseCode;
        public final String courseName;
        public final String lecturerId;
        public final String lecturerName;
        public final String title;
        public final String description;
        public final String fileName;
        public final String filePath;
        public final String uploadedAt;
        public final long fileSizeBytes;

        public MaterialRecord(
                String materialId, String offeringId, String courseCode, String courseName,
                String lecturerId, String lecturerName, String title, String description,
                String fileName, String filePath, String uploadedAt, long fileSizeBytes
        ) {
            this.materialId = materialId != null ? materialId : "";
            this.offeringId = offeringId != null ? offeringId : "";
            this.courseCode = courseCode != null ? courseCode : "";
            this.courseName = courseName != null ? courseName : "";
            this.lecturerId = lecturerId != null ? lecturerId : "";
            this.lecturerName = lecturerName != null ? lecturerName : "";
            this.title = title != null ? title : "";
            this.description = description != null ? description : "";
            this.fileName = fileName != null ? fileName : "";
            this.filePath = filePath != null ? filePath : "";
            this.uploadedAt = uploadedAt != null ? uploadedAt : "";
            this.fileSizeBytes = fileSizeBytes;
        }

        public Object[] toTableRow() {
            return new Object[]{
                    materialId,
                    courseCode,
                    title,
                    (description == null || description.trim().isEmpty()) ? "-" : description,
                    (fileName == null || fileName.trim().isEmpty()) ? "-" : fileName,
                    (lecturerName == null || lecturerName.trim().isEmpty()) ? "-" : lecturerName,
                    (uploadedAt == null || uploadedAt.trim().isEmpty()) ? "-" : uploadedAt
            };
        }
    }

    public static class MaterialsSummary {
        public final int totalMaterials;
        public final int myUploads;
        public final int totalCourses;
        public final String storageLocation;

        public MaterialsSummary(int totalMaterials, int myUploads, int totalCourses, String storageLocation) {
            this.totalMaterials = totalMaterials;
            this.myUploads = myUploads;
            this.totalCourses = totalCourses;
            this.storageLocation = storageLocation;
        }
    }

    /**
     * Loads assigned courses for the lecturer.
     */
    public static List<CourseOfferingItem> loadAssignedCourses(String lecturerId) throws SQLException {
        List<CourseOfferingItem> list = new ArrayList<>();
        String safeId = (lecturerId != null && !lecturerId.trim().isEmpty()) ? lecturerId.trim() : "LEC001";

        String sql = "SELECT DISTINCT co.offering_id, c.course_code, c.course_name, co.academic_year, co.semester " +
                "FROM course_lecturers cl " +
                "JOIN course_offerings co ON cl.offering_id = co.offering_id " +
                "JOIN courses c ON co.course_id = c.course_id " +
                "WHERE cl.lecturer_id = ? " +
                "ORDER BY c.course_code";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, safeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new CourseOfferingItem(
                            rs.getString("offering_id"),
                            rs.getString("course_code"),
                            rs.getString("course_name"),
                            rs.getString("academic_year"),
                            rs.getString("semester")
                    ));
                }
            }
        }
        return list;
    }

    /**
     * Loads all course materials uploaded for the lecturer's assigned courses.
     */
    public static List<MaterialRecord> loadMaterials(String lecturerId) throws SQLException {
        List<MaterialRecord> list = new ArrayList<>();
        String safeId = (lecturerId != null && !lecturerId.trim().isEmpty()) ? lecturerId.trim() : "LEC001";

        String sql = "SELECT DISTINCT cm.material_id, cm.offering_id, c.course_code, c.course_name, " +
                "       cm.lecturer_id, u.first_name, u.last_name, " +
                "       cm.title, cm.description, cm.file_name, cm.file_path, cm.uploaded_at " +
                "FROM course_materials cm " +
                "JOIN course_offerings co ON cm.offering_id = co.offering_id " +
                "JOIN courses c ON co.course_id = c.course_id " +
                "JOIN course_lecturers cl ON co.offering_id = cl.offering_id " +
                "LEFT JOIN users u ON cm.lecturer_id = u.user_id " +
                "WHERE cl.lecturer_id = ? " +
                "ORDER BY cm.uploaded_at DESC";

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, safeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String matId = rs.getString("material_id");
                    String offId = rs.getString("offering_id");
                    String code = rs.getString("course_code");
                    String cname = rs.getString("course_name");
                    String lecId = rs.getString("lecturer_id");
                    String fn = rs.getString("first_name");
                    String ln = rs.getString("last_name");
                    String lecName = ((fn != null ? fn : "") + " " + (ln != null ? ln : "")).trim();
                    if (lecName.isEmpty()) lecName = lecId;

                    String title = rs.getString("title");
                    String desc = rs.getString("description");
                    String fileName = rs.getString("file_name");
                    String filePath = rs.getString("file_path");

                    Timestamp ts = rs.getTimestamp("uploaded_at");
                    String uploadedStr = ts != null ? sdf.format(ts) : "-";

                    long fsize = 0;
                    if (filePath != null && !filePath.isEmpty()) {
                        File f = resolveFile(filePath);
                        if (f != null && f.exists()) {
                            fsize = f.length();
                        }
                    }

                    list.add(new MaterialRecord(
                            matId, offId, code, cname,
                            lecId, lecName, title, desc,
                            fileName, filePath, uploadedStr, fsize
                    ));
                }
            }
        }
        return list;
    }

    /**
     * Saves a new course material record and copies uploaded file into materials/ folder.
     */
    public static String saveMaterial(
            String offeringId, String title, String description,
            File sourceFile, String lecturerId
    ) throws SQLException, IOException {
        String newId;
        String storedPath = null;
        String fileName = null;

        try (Connection conn = DBConnection.getConnection()) {
            newId = getNextMaterialId(conn);

            if (sourceFile != null && sourceFile.exists()) {
                fileName = sourceFile.getName();
                File materialsDir = new File("materials");
                if (!materialsDir.exists()) materialsDir.mkdirs();

                String uniqueName = newId + "_" + fileName;
                File dest = new File(materialsDir, uniqueName);
                Files.copy(sourceFile.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                storedPath = "materials/" + uniqueName;
            }

            String insertSql = "INSERT INTO course_materials (material_id, offering_id, lecturer_id, title, description, file_name, file_path, uploaded_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, NOW())";
            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                ps.setString(1, newId);
                ps.setString(2, offeringId != null ? offeringId.trim() : "");
                ps.setString(3, lecturerId != null ? lecturerId.trim() : "LEC001");
                ps.setString(4, title != null ? title.trim() : "");
                ps.setString(5, description != null ? description.trim() : "");
                ps.setString(6, fileName);
                ps.setString(7, storedPath);
                ps.executeUpdate();
            }
        }
        return newId;
    }

    /**
     * Updates an existing course material record.
     */
    public static boolean updateMaterial(
            String materialId, String offeringId, String title,
            String description, File sourceFile, String lecturerId
    ) throws SQLException, IOException {
        String storedPath = null;
        String fileName = null;

        if (sourceFile != null && sourceFile.exists()) {
            fileName = sourceFile.getName();
            File materialsDir = new File("materials");
            if (!materialsDir.exists()) materialsDir.mkdirs();

            String uniqueName = materialId + "_" + fileName;
            File dest = new File(materialsDir, uniqueName);
            Files.copy(sourceFile.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
            storedPath = "materials/" + uniqueName;
        }

        try (Connection conn = DBConnection.getConnection()) {
            StringBuilder sql = new StringBuilder("UPDATE course_materials SET title = ?, description = ?");
            if (offeringId != null && !offeringId.trim().isEmpty()) {
                sql.append(", offering_id = ?");
            }
            if (storedPath != null) {
                sql.append(", file_name = ?, file_path = ?");
            }
            sql.append(" WHERE material_id = ?");

            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                int p = 1;
                ps.setString(p++, title != null ? title.trim() : "");
                ps.setString(p++, description != null ? description.trim() : "");
                if (offeringId != null && !offeringId.trim().isEmpty()) {
                    ps.setString(p++, offeringId.trim());
                }
                if (storedPath != null) {
                    ps.setString(p++, fileName);
                    ps.setString(p++, storedPath);
                }
                ps.setString(p, materialId.trim());

                return ps.executeUpdate() > 0;
            }
        }
    }

    /**
     * Deletes a material from database and local storage.
     */
    public static boolean deleteMaterial(String materialId, String lecturerId) throws SQLException {
        if (materialId == null || materialId.trim().isEmpty()) return false;

        try (Connection conn = DBConnection.getConnection()) {
            String selSql = "SELECT file_path FROM course_materials WHERE material_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(selSql)) {
                ps.setString(1, materialId.trim());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String path = rs.getString("file_path");
                        if (path != null && !path.isEmpty()) {
                            File f = resolveFile(path);
                            if (f != null && f.exists()) f.delete();
                        }
                    }
                }
            }

            String delSql = "DELETE FROM course_materials WHERE material_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(delSql)) {
                ps.setString(1, materialId.trim());
                return ps.executeUpdate() > 0;
            }
        }
    }

    /**
     * Filters course materials list.
     */
    public static List<MaterialRecord> filterMaterials(
            List<MaterialRecord> loaded,
            String filterCourseCode,
            String searchQuery
    ) {
        List<MaterialRecord> result = new ArrayList<>();
        if (loaded == null) return result;

        String query = searchQuery != null ? searchQuery.trim().toLowerCase(Locale.ROOT) : "";

        for (MaterialRecord m : loaded) {
            boolean matchesCourse = (filterCourseCode == null || filterCourseCode.equals("All Courses") || filterCourseCode.isEmpty())
                    || m.courseCode.equalsIgnoreCase(filterCourseCode);

            boolean matchesSearch = query.isEmpty()
                    || m.title.toLowerCase(Locale.ROOT).contains(query)
                    || m.courseCode.toLowerCase(Locale.ROOT).contains(query)
                    || m.courseName.toLowerCase(Locale.ROOT).contains(query)
                    || m.fileName.toLowerCase(Locale.ROOT).contains(query)
                    || m.description.toLowerCase(Locale.ROOT).contains(query);

            if (matchesCourse && matchesSearch) {
                result.add(m);
            }
        }
        return result;
    }

    /**
     * Calculates summary metrics for course materials.
     */
    public static MaterialsSummary calculateSummary(List<MaterialRecord> list, int totalCourses, String lecturerId) {
        if (list == null || list.isEmpty()) {
            return new MaterialsSummary(0, 0, totalCourses, "materials/");
        }
        int myCount = 0;
        String safeLec = (lecturerId != null) ? lecturerId.trim() : "";
        for (MaterialRecord m : list) {
            if (safeLec.isEmpty() || m.lecturerId.equalsIgnoreCase(safeLec)) {
                myCount++;
            }
        }
        return new MaterialsSummary(list.size(), myCount, totalCourses, "materials/");
    }

    public static File resolveFile(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) return null;
        File f = new File(filePath);
        if (f.exists()) return f;
        f = new File("materials", f.getName());
        if (f.exists()) return f;
        f = new File("uploads/course_materials", f.getName());
        if (f.exists()) return f;
        return null;
    }

    private static String getNextMaterialId(Connection conn) throws SQLException {
        String sql = "SELECT COALESCE(MAX(CAST(SUBSTRING(material_id, 4) AS UNSIGNED)), 0) + 1 FROM course_materials WHERE material_id LIKE 'MAT%'";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                long nextNum = rs.getLong(1);
                return String.format(Locale.US, "MAT%03d", nextNum);
            }
        }
        return "MAT001";
    }
}

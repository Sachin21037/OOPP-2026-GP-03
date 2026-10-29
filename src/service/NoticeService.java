package service;

import dao.NoticeDAO;
import model.Notice;

import java.util.List;

/**
 * Service coordinating Academic Notices business operations.
 * Enforces view-only access, department filtering, unread status management,
 * and search functionality.
 */
public class NoticeService {

    private final NoticeDAO noticeDAO;

    public NoticeService() {
        this.noticeDAO = new NoticeDAO();
    }

    public NoticeService(NoticeDAO noticeDAO) {
        this.noticeDAO = noticeDAO;
    }

    public List<Notice> getAllNotices() {
        return noticeDAO.getAllNotices();
    }

    public List<Notice> getActiveNotices() {
        return noticeDAO.getActiveNotices();
    }

    public List<Notice> getDepartmentNotices(String department, String statusMode) {
        return noticeDAO.getDepartmentNotices(department, statusMode);
    }

    public List<Notice> searchNotices(String keyword) {
        return noticeDAO.searchNotices(keyword, null, "ALL");
    }

    public List<Notice> searchNotices(String keyword, String department, String statusMode) {
        return noticeDAO.searchNotices(keyword, department, statusMode);
    }

    public Notice getNoticeById(String id) {
        return noticeDAO.getNoticeById(id);
    }

    public void markNoticeAsRead(String noticeId) {
        noticeDAO.markNoticeAsRead(noticeId);
    }

    public int getUnreadCount(String department) {
        return noticeDAO.getUnreadCount(department);
    }
}

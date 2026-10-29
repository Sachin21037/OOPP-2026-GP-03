package service;

import dao.NoticeDAO;
import model.Notice;

import java.util.List;

/**
 * Service coordinating Academic Notices business operations.
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

    public List<Notice> searchNotices(String keyword) {
        return noticeDAO.searchNotices(keyword);
    }

    public Notice getNoticeById(String id) {
        return noticeDAO.getNoticeById(id);
    }
}

package service;

import dao.MedicalDAO;
import model.Medical;

import java.util.List;

/**
 * MedicalService Class
 * Business logic service layer for Technical Officer Medical Certificate operations.
 * Coordinates medical certificate monitoring, verification, approval, rejection, and addition.
 */
public class MedicalService {

    private final MedicalDAO medicalDAO;

    public MedicalService() {
        this.medicalDAO = new MedicalDAO();
    }

    public List<Medical> getAllMedicals() {
        return medicalDAO.getAllMedicals();
    }

    public List<Medical> getMedicalsByStudent(int studentId) {
        return medicalDAO.getMedicalsByStudent(studentId);
    }

    public List<Medical> getMedicalsByStudentCode(String studentCode) {
        return medicalDAO.getMedicalsByStudentCode(studentCode);
    }

    public int getApprovedMedicalCount(String studentCode) {
        return medicalDAO.getApprovedMedicalCount(studentCode);
    }

    public boolean hasSubmittedMedical(String studentCode) {
        return medicalDAO.hasSubmittedMedical(studentCode);
    }

    public int getPendingMedicalCount() {
        return medicalDAO.getPendingMedicalCount();
    }

    public boolean approveMedical(String medicalCode, String approvedBy) {
        return medicalDAO.approveMedical(medicalCode, approvedBy);
    }

    public boolean rejectMedical(String medicalCode, String approvedBy) {
        return medicalDAO.rejectMedical(medicalCode, approvedBy);
    }

    public boolean addMedical(Medical medical) {
        return medicalDAO.addMedical(medical);
    }

    public boolean deleteMedical(String medicalCode) {
        return medicalDAO.deleteMedical(medicalCode);
    }
}

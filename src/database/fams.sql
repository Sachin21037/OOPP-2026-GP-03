-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Oct 14, 2026 at 04:42 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `fams`
--

CREATE DATABASE IF NOT EXISTS `fams` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `fams`;

-- --------------------------------------------------------

--
-- Table structure for table `admins`
--

CREATE TABLE `admins` (
  `admin_id` varchar(10) NOT NULL,
  `department` varchar(100) DEFAULT 'Faculty of Technology'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `admins`
--

INSERT INTO `admins` (`admin_id`, `department`) VALUES
('ADM001', 'Faculty of Technology');

-- --------------------------------------------------------

--
-- Table structure for table `assessments`
--

CREATE TABLE `assessments` (
  `assessment_id` varchar(10) NOT NULL,
  `offering_id` varchar(10) NOT NULL,
  `assessment_name` varchar(100) NOT NULL,
  `assessment_type` enum('QUIZ','ASSIGNMENT','MID_EXAM','FINAL_EXAM','OTHER') NOT NULL,
  `weight_percentage` decimal(5,2) NOT NULL,
  `max_marks` decimal(5,2) DEFAULT 100.00,
  `assessment_date` date DEFAULT NULL,
  `created_by` varchar(10) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `assessments`
--

INSERT INTO `assessments` (`assessment_id`, `offering_id`, `assessment_name`, `assessment_type`, `weight_percentage`, `max_marks`, `assessment_date`, `created_by`) VALUES
('ASM001', 'OFF001', 'Quiz 01', 'QUIZ', 10.00, 100.00, '2026-02-05', 'LEC001'),
('ASM002', 'OFF001', 'Assignment 01', 'ASSIGNMENT', 10.00, 100.00, '2026-02-20', 'LEC001'),
('ASM003', 'OFF001', 'Mid Semester Examination', 'MID_EXAM', 25.00, 100.00, '2026-03-10', 'LEC001'),
('ASM004', 'OFF001', 'Final Examination', 'FINAL_EXAM', 55.00, 100.00, '2026-05-20', 'LEC001'),
('ASM005', 'OFF002', 'Quiz 01', 'QUIZ', 10.00, 100.00, '2026-02-06', 'LEC002'),
('ASM006', 'OFF002', 'Assignment 01', 'ASSIGNMENT', 10.00, 100.00, '2026-02-21', 'LEC002'),
('ASM007', 'OFF002', 'Mid Semester Examination', 'MID_EXAM', 25.00, 100.00, '2026-03-11', 'LEC002'),
('ASM008', 'OFF002', 'Final Examination', 'FINAL_EXAM', 55.00, 100.00, '2026-05-21', 'LEC002'),
('ASM009', 'OFF003', 'Quiz 01', 'QUIZ', 10.00, 100.00, '2026-02-07', 'LEC003'),
('ASM010', 'OFF003', 'Assignment 01', 'ASSIGNMENT', 10.00, 100.00, '2026-02-22', 'LEC003'),
('ASM011', 'OFF003', 'Mid Semester Examination', 'MID_EXAM', 25.00, 100.00, '2026-03-12', 'LEC003'),
('ASM012', 'OFF003', 'Final Examination', 'FINAL_EXAM', 55.00, 100.00, '2026-05-22', 'LEC003');

-- --------------------------------------------------------

--
-- Table structure for table `attendance`
--

CREATE TABLE `attendance` (
  `attendance_id` varchar(10) NOT NULL,
  `session_id` varchar(10) NOT NULL,
  `student_id` varchar(10) NOT NULL,
  `status` enum('PRESENT','ABSENT','MEDICAL') NOT NULL,
  `remarks` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `attendance`
--

INSERT INTO `attendance` (`attendance_id`, `session_id`, `student_id`, `status`, `remarks`) VALUES
('ATT0001', 'SES001', 'STU001', 'PRESENT', ''),
('ATT0002', 'SES002', 'STU001', 'PRESENT', ''),
('ATT0003', 'SES003', 'STU001', 'PRESENT', ''),
('ATT0004', 'SES004', 'STU001', 'PRESENT', ''),
('ATT0005', 'SES005', 'STU001', 'PRESENT', ''),
('ATT0006', 'SES006', 'STU001', 'PRESENT', ''),
('ATT0007', 'SES007', 'STU001', 'PRESENT', ''),
('ATT0008', 'SES008', 'STU001', 'PRESENT', ''),
('ATT0009', 'SES009', 'STU001', 'PRESENT', ''),
('ATT0010', 'SES010', 'STU001', 'PRESENT', ''),
('ATT0011', 'SES011', 'STU001', 'PRESENT', ''),
('ATT0012', 'SES012', 'STU001', 'PRESENT', ''),
('ATT0013', 'SES013', 'STU001', 'PRESENT', ''),
('ATT0014', 'SES014', 'STU001', 'PRESENT', ''),
('ATT0015', 'SES015', 'STU001', 'PRESENT', ''),
('ATT0016', 'SES016', 'STU001', 'PRESENT', ''),
('ATT0017', 'SES017', 'STU001', 'PRESENT', ''),
('ATT0018', 'SES018', 'STU001', 'PRESENT', ''),
('ATT0019', 'SES019', 'STU001', 'PRESENT', ''),
('ATT0020', 'SES020', 'STU001', 'PRESENT', ''),
('ATT0021', 'SES021', 'STU001', 'PRESENT', ''),
('ATT0022', 'SES022', 'STU001', 'PRESENT', ''),
('ATT0023', 'SES023', 'STU001', 'PRESENT', ''),
('ATT0024', 'SES024', 'STU001', 'PRESENT', ''),
('ATT0025', 'SES025', 'STU001', 'PRESENT', ''),
('ATT0026', 'SES026', 'STU001', 'PRESENT', ''),
('ATT0027', 'SES027', 'STU001', 'PRESENT', ''),
('ATT0028', 'SES028', 'STU001', 'PRESENT', ''),
('ATT0029', 'SES029', 'STU001', 'PRESENT', ''),
('ATT0030', 'SES030', 'STU001', 'PRESENT', ''),
('ATT0031', 'SES001', 'STU002', 'PRESENT', ''),
('ATT0032', 'SES002', 'STU002', 'PRESENT', ''),
('ATT0033', 'SES003', 'STU002', 'PRESENT', ''),
('ATT0034', 'SES004', 'STU002', 'PRESENT', ''),
('ATT0035', 'SES005', 'STU002', 'PRESENT', ''),
('ATT0036', 'SES006', 'STU002', 'PRESENT', ''),
('ATT0037', 'SES007', 'STU002', 'PRESENT', ''),
('ATT0038', 'SES008', 'STU002', 'PRESENT', ''),
('ATT0039', 'SES009', 'STU002', 'PRESENT', ''),
('ATT0040', 'SES010', 'STU002', 'PRESENT', ''),
('ATT0041', 'SES011', 'STU002', 'PRESENT', ''),
('ATT0042', 'SES012', 'STU002', 'PRESENT', ''),
('ATT0043', 'SES013', 'STU002', 'PRESENT', ''),
('ATT0044', 'SES014', 'STU002', 'PRESENT', ''),
('ATT0045', 'SES015', 'STU002', 'PRESENT', ''),
('ATT0046', 'SES016', 'STU002', 'PRESENT', ''),
('ATT0047', 'SES017', 'STU002', 'PRESENT', ''),
('ATT0048', 'SES018', 'STU002', 'PRESENT', ''),
('ATT0049', 'SES019', 'STU002', 'PRESENT', ''),
('ATT0050', 'SES020', 'STU002', 'PRESENT', ''),
('ATT0051', 'SES021', 'STU002', 'PRESENT', ''),
('ATT0052', 'SES022', 'STU002', 'PRESENT', ''),
('ATT0053', 'SES023', 'STU002', 'PRESENT', ''),
('ATT0054', 'SES024', 'STU002', 'PRESENT', ''),
('ATT0055', 'SES025', 'STU002', 'PRESENT', ''),
('ATT0056', 'SES026', 'STU002', 'PRESENT', ''),
('ATT0057', 'SES027', 'STU002', 'PRESENT', ''),
('ATT0058', 'SES028', 'STU002', 'ABSENT', 'Absent'),
('ATT0059', 'SES029', 'STU002', 'ABSENT', 'Absent'),
('ATT0060', 'SES030', 'STU002', 'ABSENT', 'Absent'),
('ATT0061', 'SES001', 'STU003', 'PRESENT', ''),
('ATT0062', 'SES002', 'STU003', 'PRESENT', ''),
('ATT0063', 'SES003', 'STU003', 'PRESENT', ''),
('ATT0064', 'SES004', 'STU003', 'PRESENT', ''),
('ATT0065', 'SES005', 'STU003', 'PRESENT', ''),
('ATT0066', 'SES006', 'STU003', 'PRESENT', ''),
('ATT0067', 'SES007', 'STU003', 'PRESENT', ''),
('ATT0068', 'SES008', 'STU003', 'PRESENT', ''),
('ATT0069', 'SES009', 'STU003', 'PRESENT', ''),
('ATT0070', 'SES010', 'STU003', 'PRESENT', ''),
('ATT0071', 'SES011', 'STU003', 'PRESENT', ''),
('ATT0072', 'SES012', 'STU003', 'PRESENT', ''),
('ATT0073', 'SES013', 'STU003', 'PRESENT', ''),
('ATT0074', 'SES014', 'STU003', 'PRESENT', ''),
('ATT0075', 'SES015', 'STU003', 'PRESENT', ''),
('ATT0076', 'SES016', 'STU003', 'PRESENT', ''),
('ATT0077', 'SES017', 'STU003', 'PRESENT', ''),
('ATT0078', 'SES018', 'STU003', 'PRESENT', ''),
('ATT0079', 'SES019', 'STU003', 'PRESENT', ''),
('ATT0080', 'SES020', 'STU003', 'PRESENT', ''),
('ATT0081', 'SES021', 'STU003', 'PRESENT', ''),
('ATT0082', 'SES022', 'STU003', 'PRESENT', ''),
('ATT0083', 'SES023', 'STU003', 'PRESENT', ''),
('ATT0084', 'SES024', 'STU003', 'PRESENT', ''),
('ATT0085', 'SES025', 'STU003', 'ABSENT', 'Absent'),
('ATT0086', 'SES026', 'STU003', 'ABSENT', 'Absent'),
('ATT0087', 'SES027', 'STU003', 'ABSENT', 'Absent'),
('ATT0088', 'SES028', 'STU003', 'ABSENT', 'Absent'),
('ATT0089', 'SES029', 'STU003', 'ABSENT', 'Absent'),
('ATT0090', 'SES030', 'STU003', 'ABSENT', 'Absent'),
('ATT0091', 'SES001', 'STU004', 'PRESENT', ''),
('ATT0092', 'SES002', 'STU004', 'PRESENT', ''),
('ATT0093', 'SES003', 'STU004', 'PRESENT', ''),
('ATT0094', 'SES004', 'STU004', 'PRESENT', ''),
('ATT0095', 'SES005', 'STU004', 'PRESENT', ''),
('ATT0096', 'SES006', 'STU004', 'PRESENT', ''),
('ATT0097', 'SES007', 'STU004', 'PRESENT', ''),
('ATT0098', 'SES008', 'STU004', 'PRESENT', ''),
('ATT0099', 'SES009', 'STU004', 'PRESENT', ''),
('ATT0100', 'SES010', 'STU004', 'PRESENT', ''),
('ATT0101', 'SES011', 'STU004', 'PRESENT', ''),
('ATT0102', 'SES012', 'STU004', 'PRESENT', ''),
('ATT0103', 'SES013', 'STU004', 'PRESENT', ''),
('ATT0104', 'SES014', 'STU004', 'PRESENT', ''),
('ATT0105', 'SES015', 'STU004', 'PRESENT', ''),
('ATT0106', 'SES016', 'STU004', 'PRESENT', ''),
('ATT0107', 'SES017', 'STU004', 'PRESENT', ''),
('ATT0108', 'SES018', 'STU004', 'PRESENT', ''),
('ATT0109', 'SES019', 'STU004', 'PRESENT', ''),
('ATT0110', 'SES020', 'STU004', 'PRESENT', ''),
('ATT0111', 'SES021', 'STU004', 'PRESENT', ''),
('ATT0112', 'SES022', 'STU004', 'ABSENT', 'Absent'),
('ATT0113', 'SES023', 'STU004', 'ABSENT', 'Absent'),
('ATT0114', 'SES024', 'STU004', 'ABSENT', 'Absent'),
('ATT0115', 'SES025', 'STU004', 'ABSENT', 'Absent'),
('ATT0116', 'SES026', 'STU004', 'ABSENT', 'Absent'),
('ATT0117', 'SES027', 'STU004', 'ABSENT', 'Absent'),
('ATT0118', 'SES028', 'STU004', 'ABSENT', 'Absent'),
('ATT0119', 'SES029', 'STU004', 'ABSENT', 'Absent'),
('ATT0120', 'SES030', 'STU004', 'ABSENT', 'Absent'),
('ATT0121', 'SES001', 'STU005', 'PRESENT', ''),
('ATT0122', 'SES002', 'STU005', 'PRESENT', ''),
('ATT0123', 'SES003', 'STU005', 'PRESENT', ''),
('ATT0124', 'SES004', 'STU005', 'PRESENT', ''),
('ATT0125', 'SES005', 'STU005', 'MEDICAL', 'Approved medical submission'),
('ATT0126', 'SES006', 'STU005', 'PRESENT', ''),
('ATT0127', 'SES007', 'STU005', 'PRESENT', ''),
('ATT0128', 'SES008', 'STU005', 'PRESENT', ''),
('ATT0129', 'SES009', 'STU005', 'PRESENT', ''),
('ATT0130', 'SES010', 'STU005', 'PRESENT', ''),
('ATT0131', 'SES011', 'STU005', 'PRESENT', ''),
('ATT0132', 'SES012', 'STU005', 'PRESENT', ''),
('ATT0133', 'SES013', 'STU005', 'PRESENT', ''),
('ATT0134', 'SES014', 'STU005', 'PRESENT', ''),
('ATT0135', 'SES015', 'STU005', 'PRESENT', ''),
('ATT0136', 'SES016', 'STU005', 'PRESENT', ''),
('ATT0137', 'SES017', 'STU005', 'PRESENT', ''),
('ATT0138', 'SES018', 'STU005', 'PRESENT', ''),
('ATT0139', 'SES019', 'STU005', 'PRESENT', ''),
('ATT0140', 'SES020', 'STU005', 'PRESENT', ''),
('ATT0141', 'SES021', 'STU005', 'PRESENT', ''),
('ATT0142', 'SES022', 'STU005', 'PRESENT', ''),
('ATT0143', 'SES023', 'STU005', 'PRESENT', ''),
('ATT0144', 'SES024', 'STU005', 'PRESENT', ''),
('ATT0145', 'SES025', 'STU005', 'PRESENT', ''),
('ATT0146', 'SES026', 'STU005', 'PRESENT', ''),
('ATT0147', 'SES027', 'STU005', 'PRESENT', ''),
('ATT0148', 'SES028', 'STU005', 'ABSENT', 'Absent'),
('ATT0149', 'SES029', 'STU005', 'ABSENT', 'Absent'),
('ATT0150', 'SES030', 'STU005', 'ABSENT', 'Absent'),
('ATT0151', 'SES001', 'STU006', 'PRESENT', ''),
('ATT0152', 'SES002', 'STU006', 'PRESENT', ''),
('ATT0153', 'SES003', 'STU006', 'PRESENT', ''),
('ATT0154', 'SES004', 'STU006', 'PRESENT', ''),
('ATT0155', 'SES005', 'STU006', 'PRESENT', ''),
('ATT0156', 'SES006', 'STU006', 'PRESENT', ''),
('ATT0157', 'SES007', 'STU006', 'PRESENT', ''),
('ATT0158', 'SES008', 'STU006', 'PRESENT', ''),
('ATT0159', 'SES009', 'STU006', 'PRESENT', ''),
('ATT0160', 'SES010', 'STU006', 'PRESENT', ''),
('ATT0161', 'SES011', 'STU006', 'PRESENT', ''),
('ATT0162', 'SES012', 'STU006', 'PRESENT', ''),
('ATT0163', 'SES013', 'STU006', 'PRESENT', ''),
('ATT0164', 'SES014', 'STU006', 'PRESENT', ''),
('ATT0165', 'SES015', 'STU006', 'PRESENT', ''),
('ATT0166', 'SES016', 'STU006', 'PRESENT', ''),
('ATT0167', 'SES017', 'STU006', 'PRESENT', ''),
('ATT0168', 'SES018', 'STU006', 'PRESENT', ''),
('ATT0169', 'SES019', 'STU006', 'PRESENT', ''),
('ATT0170', 'SES020', 'STU006', 'PRESENT', ''),
('ATT0171', 'SES021', 'STU006', 'PRESENT', ''),
('ATT0172', 'SES022', 'STU006', 'PRESENT', ''),
('ATT0173', 'SES023', 'STU006', 'PRESENT', ''),
('ATT0174', 'SES024', 'STU006', 'PRESENT', ''),
('ATT0175', 'SES025', 'STU006', 'PRESENT', ''),
('ATT0176', 'SES026', 'STU006', 'PRESENT', ''),
('ATT0177', 'SES027', 'STU006', 'PRESENT', ''),
('ATT0178', 'SES028', 'STU006', 'ABSENT', 'Absent'),
('ATT0179', 'SES029', 'STU006', 'ABSENT', 'Absent'),
('ATT0180', 'SES030', 'STU006', 'ABSENT', 'Absent'),
('ATT0181', 'SES001', 'STU007', 'PRESENT', ''),
('ATT0182', 'SES002', 'STU007', 'PRESENT', ''),
('ATT0183', 'SES003', 'STU007', 'PRESENT', ''),
('ATT0184', 'SES004', 'STU007', 'PRESENT', ''),
('ATT0185', 'SES005', 'STU007', 'PRESENT', ''),
('ATT0186', 'SES006', 'STU007', 'PRESENT', ''),
('ATT0187', 'SES007', 'STU007', 'PRESENT', ''),
('ATT0188', 'SES008', 'STU007', 'PRESENT', ''),
('ATT0189', 'SES009', 'STU007', 'PRESENT', ''),
('ATT0190', 'SES010', 'STU007', 'PRESENT', ''),
('ATT0191', 'SES011', 'STU007', 'PRESENT', ''),
('ATT0192', 'SES012', 'STU007', 'PRESENT', ''),
('ATT0193', 'SES013', 'STU007', 'PRESENT', ''),
('ATT0194', 'SES014', 'STU007', 'PRESENT', ''),
('ATT0195', 'SES015', 'STU007', 'PRESENT', ''),
('ATT0196', 'SES016', 'STU007', 'PRESENT', ''),
('ATT0197', 'SES017', 'STU007', 'PRESENT', ''),
('ATT0198', 'SES018', 'STU007', 'PRESENT', ''),
('ATT0199', 'SES019', 'STU007', 'PRESENT', ''),
('ATT0200', 'SES020', 'STU007', 'PRESENT', ''),
('ATT0201', 'SES021', 'STU007', 'PRESENT', ''),
('ATT0202', 'SES022', 'STU007', 'PRESENT', ''),
('ATT0203', 'SES023', 'STU007', 'PRESENT', ''),
('ATT0204', 'SES024', 'STU007', 'PRESENT', ''),
('ATT0205', 'SES025', 'STU007', 'PRESENT', ''),
('ATT0206', 'SES026', 'STU007', 'PRESENT', ''),
('ATT0207', 'SES027', 'STU007', 'PRESENT', ''),
('ATT0208', 'SES028', 'STU007', 'ABSENT', 'Absent'),
('ATT0209', 'SES029', 'STU007', 'ABSENT', 'Absent'),
('ATT0210', 'SES030', 'STU007', 'ABSENT', 'Absent'),
('ATT0211', 'SES001', 'STU008', 'PRESENT', ''),
('ATT0212', 'SES002', 'STU008', 'PRESENT', ''),
('ATT0213', 'SES003', 'STU008', 'PRESENT', ''),
('ATT0214', 'SES004', 'STU008', 'PRESENT', ''),
('ATT0215', 'SES005', 'STU008', 'PRESENT', ''),
('ATT0216', 'SES006', 'STU008', 'PRESENT', ''),
('ATT0217', 'SES007', 'STU008', 'PRESENT', ''),
('ATT0218', 'SES008', 'STU008', 'PRESENT', ''),
('ATT0219', 'SES009', 'STU008', 'PRESENT', ''),
('ATT0220', 'SES010', 'STU008', 'PRESENT', ''),
('ATT0221', 'SES011', 'STU008', 'PRESENT', ''),
('ATT0222', 'SES012', 'STU008', 'PRESENT', ''),
('ATT0223', 'SES013', 'STU008', 'PRESENT', ''),
('ATT0224', 'SES014', 'STU008', 'PRESENT', ''),
('ATT0225', 'SES015', 'STU008', 'PRESENT', ''),
('ATT0226', 'SES016', 'STU008', 'PRESENT', ''),
('ATT0227', 'SES017', 'STU008', 'PRESENT', ''),
('ATT0228', 'SES018', 'STU008', 'PRESENT', ''),
('ATT0229', 'SES019', 'STU008', 'PRESENT', ''),
('ATT0230', 'SES020', 'STU008', 'PRESENT', ''),
('ATT0231', 'SES021', 'STU008', 'PRESENT', ''),
('ATT0232', 'SES022', 'STU008', 'PRESENT', ''),
('ATT0233', 'SES023', 'STU008', 'PRESENT', ''),
('ATT0234', 'SES024', 'STU008', 'PRESENT', ''),
('ATT0235', 'SES025', 'STU008', 'PRESENT', ''),
('ATT0236', 'SES026', 'STU008', 'PRESENT', ''),
('ATT0237', 'SES027', 'STU008', 'PRESENT', ''),
('ATT0238', 'SES028', 'STU008', 'ABSENT', 'Absent'),
('ATT0239', 'SES029', 'STU008', 'ABSENT', 'Absent'),
('ATT0240', 'SES030', 'STU008', 'ABSENT', 'Absent'),
('ATT0241', 'SES001', 'STU009', 'PRESENT', ''),
('ATT0242', 'SES002', 'STU009', 'PRESENT', ''),
('ATT0243', 'SES003', 'STU009', 'PRESENT', ''),
('ATT0244', 'SES004', 'STU009', 'PRESENT', ''),
('ATT0245', 'SES005', 'STU009', 'PRESENT', ''),
('ATT0246', 'SES006', 'STU009', 'PRESENT', ''),
('ATT0247', 'SES007', 'STU009', 'PRESENT', ''),
('ATT0248', 'SES008', 'STU009', 'PRESENT', ''),
('ATT0249', 'SES009', 'STU009', 'PRESENT', ''),
('ATT0250', 'SES010', 'STU009', 'PRESENT', ''),
('ATT0251', 'SES011', 'STU009', 'PRESENT', ''),
('ATT0252', 'SES012', 'STU009', 'PRESENT', ''),
('ATT0253', 'SES013', 'STU009', 'PRESENT', ''),
('ATT0254', 'SES014', 'STU009', 'PRESENT', ''),
('ATT0255', 'SES015', 'STU009', 'PRESENT', ''),
('ATT0256', 'SES016', 'STU009', 'PRESENT', ''),
('ATT0257', 'SES017', 'STU009', 'PRESENT', ''),
('ATT0258', 'SES018', 'STU009', 'PRESENT', ''),
('ATT0259', 'SES019', 'STU009', 'PRESENT', ''),
('ATT0260', 'SES020', 'STU009', 'PRESENT', ''),
('ATT0261', 'SES021', 'STU009', 'PRESENT', ''),
('ATT0262', 'SES022', 'STU009', 'PRESENT', ''),
('ATT0263', 'SES023', 'STU009', 'PRESENT', ''),
('ATT0264', 'SES024', 'STU009', 'PRESENT', ''),
('ATT0265', 'SES025', 'STU009', 'PRESENT', ''),
('ATT0266', 'SES026', 'STU009', 'PRESENT', ''),
('ATT0267', 'SES027', 'STU009', 'PRESENT', ''),
('ATT0268', 'SES028', 'STU009', 'ABSENT', 'Absent'),
('ATT0269', 'SES029', 'STU009', 'ABSENT', 'Absent'),
('ATT0270', 'SES030', 'STU009', 'ABSENT', 'Absent'),
('ATT0271', 'SES001', 'STU010', 'PRESENT', ''),
('ATT0272', 'SES002', 'STU010', 'PRESENT', ''),
('ATT0273', 'SES003', 'STU010', 'PRESENT', ''),
('ATT0274', 'SES004', 'STU010', 'PRESENT', ''),
('ATT0275', 'SES005', 'STU010', 'PRESENT', ''),
('ATT0276', 'SES006', 'STU010', 'PRESENT', ''),
('ATT0277', 'SES007', 'STU010', 'PRESENT', ''),
('ATT0278', 'SES008', 'STU010', 'PRESENT', ''),
('ATT0279', 'SES009', 'STU010', 'PRESENT', ''),
('ATT0280', 'SES010', 'STU010', 'PRESENT', ''),
('ATT0281', 'SES011', 'STU010', 'PRESENT', ''),
('ATT0282', 'SES012', 'STU010', 'PRESENT', ''),
('ATT0283', 'SES013', 'STU010', 'PRESENT', ''),
('ATT0284', 'SES014', 'STU010', 'PRESENT', ''),
('ATT0285', 'SES015', 'STU010', 'PRESENT', ''),
('ATT0286', 'SES016', 'STU010', 'PRESENT', ''),
('ATT0287', 'SES017', 'STU010', 'PRESENT', ''),
('ATT0288', 'SES018', 'STU010', 'PRESENT', ''),
('ATT0289', 'SES019', 'STU010', 'PRESENT', ''),
('ATT0290', 'SES020', 'STU010', 'PRESENT', ''),
('ATT0291', 'SES021', 'STU010', 'PRESENT', ''),
('ATT0292', 'SES022', 'STU010', 'PRESENT', ''),
('ATT0293', 'SES023', 'STU010', 'PRESENT', ''),
('ATT0294', 'SES024', 'STU010', 'PRESENT', ''),
('ATT0295', 'SES025', 'STU010', 'PRESENT', ''),
('ATT0296', 'SES026', 'STU010', 'PRESENT', ''),
('ATT0297', 'SES027', 'STU010', 'PRESENT', ''),
('ATT0298', 'SES028', 'STU010', 'ABSENT', 'Absent'),
('ATT0299', 'SES029', 'STU010', 'ABSENT', 'Absent'),
('ATT0300', 'SES030', 'STU010', 'ABSENT', 'Absent'),
('ATT0301', 'SES001', 'STU011', 'PRESENT', ''),
('ATT0302', 'SES002', 'STU011', 'PRESENT', ''),
('ATT0303', 'SES003', 'STU011', 'PRESENT', ''),
('ATT0304', 'SES004', 'STU011', 'PRESENT', ''),
('ATT0305', 'SES005', 'STU011', 'PRESENT', ''),
('ATT0306', 'SES006', 'STU011', 'PRESENT', ''),
('ATT0307', 'SES007', 'STU011', 'PRESENT', ''),
('ATT0308', 'SES008', 'STU011', 'PRESENT', ''),
('ATT0309', 'SES009', 'STU011', 'PRESENT', ''),
('ATT0310', 'SES010', 'STU011', 'PRESENT', ''),
('ATT0311', 'SES011', 'STU011', 'PRESENT', ''),
('ATT0312', 'SES012', 'STU011', 'PRESENT', ''),
('ATT0313', 'SES013', 'STU011', 'PRESENT', ''),
('ATT0314', 'SES014', 'STU011', 'PRESENT', ''),
('ATT0315', 'SES015', 'STU011', 'PRESENT', ''),
('ATT0316', 'SES016', 'STU011', 'PRESENT', ''),
('ATT0317', 'SES017', 'STU011', 'PRESENT', ''),
('ATT0318', 'SES018', 'STU011', 'PRESENT', ''),
('ATT0319', 'SES019', 'STU011', 'PRESENT', ''),
('ATT0320', 'SES020', 'STU011', 'PRESENT', ''),
('ATT0321', 'SES021', 'STU011', 'PRESENT', ''),
('ATT0322', 'SES022', 'STU011', 'PRESENT', ''),
('ATT0323', 'SES023', 'STU011', 'PRESENT', ''),
('ATT0324', 'SES024', 'STU011', 'PRESENT', ''),
('ATT0325', 'SES025', 'STU011', 'PRESENT', ''),
('ATT0326', 'SES026', 'STU011', 'PRESENT', ''),
('ATT0327', 'SES027', 'STU011', 'PRESENT', ''),
('ATT0328', 'SES028', 'STU011', 'ABSENT', 'Absent'),
('ATT0329', 'SES029', 'STU011', 'ABSENT', 'Absent'),
('ATT0330', 'SES030', 'STU011', 'ABSENT', 'Absent'),
('ATT0331', 'SES001', 'STU012', 'PRESENT', ''),
('ATT0332', 'SES002', 'STU012', 'PRESENT', ''),
('ATT0333', 'SES003', 'STU012', 'PRESENT', ''),
('ATT0334', 'SES004', 'STU012', 'PRESENT', ''),
('ATT0335', 'SES005', 'STU012', 'PRESENT', ''),
('ATT0336', 'SES006', 'STU012', 'PRESENT', ''),
('ATT0337', 'SES007', 'STU012', 'PRESENT', ''),
('ATT0338', 'SES008', 'STU012', 'PRESENT', ''),
('ATT0339', 'SES009', 'STU012', 'PRESENT', ''),
('ATT0340', 'SES010', 'STU012', 'PRESENT', ''),
('ATT0341', 'SES011', 'STU012', 'PRESENT', ''),
('ATT0342', 'SES012', 'STU012', 'PRESENT', ''),
('ATT0343', 'SES013', 'STU012', 'PRESENT', ''),
('ATT0344', 'SES014', 'STU012', 'PRESENT', ''),
('ATT0345', 'SES015', 'STU012', 'PRESENT', ''),
('ATT0346', 'SES016', 'STU012', 'PRESENT', ''),
('ATT0347', 'SES017', 'STU012', 'PRESENT', ''),
('ATT0348', 'SES018', 'STU012', 'PRESENT', ''),
('ATT0349', 'SES019', 'STU012', 'PRESENT', ''),
('ATT0350', 'SES020', 'STU012', 'PRESENT', ''),
('ATT0351', 'SES021', 'STU012', 'PRESENT', ''),
('ATT0352', 'SES022', 'STU012', 'PRESENT', ''),
('ATT0353', 'SES023', 'STU012', 'PRESENT', ''),
('ATT0354', 'SES024', 'STU012', 'PRESENT', ''),
('ATT0355', 'SES025', 'STU012', 'PRESENT', ''),
('ATT0356', 'SES026', 'STU012', 'PRESENT', ''),
('ATT0357', 'SES027', 'STU012', 'PRESENT', ''),
('ATT0358', 'SES028', 'STU012', 'ABSENT', 'Absent'),
('ATT0359', 'SES029', 'STU012', 'ABSENT', 'Absent'),
('ATT0360', 'SES030', 'STU012', 'ABSENT', 'Absent'),
('ATT0361', 'SES001', 'STU013', 'PRESENT', ''),
('ATT0362', 'SES002', 'STU013', 'PRESENT', ''),
('ATT0363', 'SES003', 'STU013', 'PRESENT', ''),
('ATT0364', 'SES004', 'STU013', 'PRESENT', ''),
('ATT0365', 'SES005', 'STU013', 'PRESENT', ''),
('ATT0366', 'SES006', 'STU013', 'PRESENT', ''),
('ATT0367', 'SES007', 'STU013', 'PRESENT', ''),
('ATT0368', 'SES008', 'STU013', 'PRESENT', ''),
('ATT0369', 'SES009', 'STU013', 'PRESENT', ''),
('ATT0370', 'SES010', 'STU013', 'PRESENT', ''),
('ATT0371', 'SES011', 'STU013', 'PRESENT', ''),
('ATT0372', 'SES012', 'STU013', 'PRESENT', ''),
('ATT0373', 'SES013', 'STU013', 'PRESENT', ''),
('ATT0374', 'SES014', 'STU013', 'PRESENT', ''),
('ATT0375', 'SES015', 'STU013', 'PRESENT', ''),
('ATT0376', 'SES016', 'STU013', 'PRESENT', ''),
('ATT0377', 'SES017', 'STU013', 'PRESENT', ''),
('ATT0378', 'SES018', 'STU013', 'PRESENT', ''),
('ATT0379', 'SES019', 'STU013', 'PRESENT', ''),
('ATT0380', 'SES020', 'STU013', 'PRESENT', ''),
('ATT0381', 'SES021', 'STU013', 'PRESENT', ''),
('ATT0382', 'SES022', 'STU013', 'PRESENT', ''),
('ATT0383', 'SES023', 'STU013', 'PRESENT', ''),
('ATT0384', 'SES024', 'STU013', 'PRESENT', ''),
('ATT0385', 'SES025', 'STU013', 'PRESENT', ''),
('ATT0386', 'SES026', 'STU013', 'PRESENT', ''),
('ATT0387', 'SES027', 'STU013', 'PRESENT', ''),
('ATT0388', 'SES028', 'STU013', 'ABSENT', 'Absent'),
('ATT0389', 'SES029', 'STU013', 'ABSENT', 'Absent'),
('ATT0390', 'SES030', 'STU013', 'ABSENT', 'Absent'),
('ATT0391', 'SES001', 'STU014', 'PRESENT', ''),
('ATT0392', 'SES002', 'STU014', 'PRESENT', ''),
('ATT0393', 'SES003', 'STU014', 'PRESENT', ''),
('ATT0394', 'SES004', 'STU014', 'PRESENT', ''),
('ATT0395', 'SES005', 'STU014', 'PRESENT', ''),
('ATT0396', 'SES006', 'STU014', 'PRESENT', ''),
('ATT0397', 'SES007', 'STU014', 'PRESENT', ''),
('ATT0398', 'SES008', 'STU014', 'PRESENT', ''),
('ATT0399', 'SES009', 'STU014', 'PRESENT', ''),
('ATT0400', 'SES010', 'STU014', 'PRESENT', ''),
('ATT0401', 'SES011', 'STU014', 'PRESENT', ''),
('ATT0402', 'SES012', 'STU014', 'PRESENT', ''),
('ATT0403', 'SES013', 'STU014', 'PRESENT', ''),
('ATT0404', 'SES014', 'STU014', 'PRESENT', ''),
('ATT0405', 'SES015', 'STU014', 'PRESENT', ''),
('ATT0406', 'SES016', 'STU014', 'PRESENT', ''),
('ATT0407', 'SES017', 'STU014', 'PRESENT', ''),
('ATT0408', 'SES018', 'STU014', 'PRESENT', ''),
('ATT0409', 'SES019', 'STU014', 'PRESENT', ''),
('ATT0410', 'SES020', 'STU014', 'PRESENT', ''),
('ATT0411', 'SES021', 'STU014', 'PRESENT', ''),
('ATT0412', 'SES022', 'STU014', 'PRESENT', ''),
('ATT0413', 'SES023', 'STU014', 'PRESENT', ''),
('ATT0414', 'SES024', 'STU014', 'PRESENT', ''),
('ATT0415', 'SES025', 'STU014', 'PRESENT', ''),
('ATT0416', 'SES026', 'STU014', 'PRESENT', ''),
('ATT0417', 'SES027', 'STU014', 'PRESENT', ''),
('ATT0418', 'SES028', 'STU014', 'ABSENT', 'Absent'),
('ATT0419', 'SES029', 'STU014', 'ABSENT', 'Absent'),
('ATT0420', 'SES030', 'STU014', 'ABSENT', 'Absent'),
('ATT0421', 'SES001', 'STU015', 'PRESENT', ''),
('ATT0422', 'SES002', 'STU015', 'PRESENT', ''),
('ATT0423', 'SES003', 'STU015', 'PRESENT', ''),
('ATT0424', 'SES004', 'STU015', 'PRESENT', ''),
('ATT0425', 'SES005', 'STU015', 'PRESENT', ''),
('ATT0426', 'SES006', 'STU015', 'PRESENT', ''),
('ATT0427', 'SES007', 'STU015', 'PRESENT', ''),
('ATT0428', 'SES008', 'STU015', 'PRESENT', ''),
('ATT0429', 'SES009', 'STU015', 'PRESENT', ''),
('ATT0430', 'SES010', 'STU015', 'PRESENT', ''),
('ATT0431', 'SES011', 'STU015', 'PRESENT', ''),
('ATT0432', 'SES012', 'STU015', 'PRESENT', ''),
('ATT0433', 'SES013', 'STU015', 'PRESENT', ''),
('ATT0434', 'SES014', 'STU015', 'PRESENT', ''),
('ATT0435', 'SES015', 'STU015', 'PRESENT', ''),
('ATT0436', 'SES016', 'STU015', 'PRESENT', ''),
('ATT0437', 'SES017', 'STU015', 'PRESENT', ''),
('ATT0438', 'SES018', 'STU015', 'PRESENT', ''),
('ATT0439', 'SES019', 'STU015', 'PRESENT', ''),
('ATT0440', 'SES020', 'STU015', 'PRESENT', ''),
('ATT0441', 'SES021', 'STU015', 'PRESENT', ''),
('ATT0442', 'SES022', 'STU015', 'PRESENT', ''),
('ATT0443', 'SES023', 'STU015', 'PRESENT', ''),
('ATT0444', 'SES024', 'STU015', 'PRESENT', ''),
('ATT0445', 'SES025', 'STU015', 'PRESENT', ''),
('ATT0446', 'SES026', 'STU015', 'PRESENT', ''),
('ATT0447', 'SES027', 'STU015', 'PRESENT', ''),
('ATT0448', 'SES028', 'STU015', 'ABSENT', 'Absent'),
('ATT0449', 'SES029', 'STU015', 'ABSENT', 'Absent'),
('ATT0450', 'SES030', 'STU015', 'ABSENT', 'Absent'),
('ATT0451', 'SES001', 'STU016', 'PRESENT', ''),
('ATT0452', 'SES002', 'STU016', 'PRESENT', ''),
('ATT0453', 'SES003', 'STU016', 'PRESENT', ''),
('ATT0454', 'SES004', 'STU016', 'PRESENT', ''),
('ATT0455', 'SES005', 'STU016', 'PRESENT', ''),
('ATT0456', 'SES006', 'STU016', 'PRESENT', ''),
('ATT0457', 'SES007', 'STU016', 'PRESENT', ''),
('ATT0458', 'SES008', 'STU016', 'PRESENT', ''),
('ATT0459', 'SES009', 'STU016', 'PRESENT', ''),
('ATT0460', 'SES010', 'STU016', 'PRESENT', ''),
('ATT0461', 'SES011', 'STU016', 'PRESENT', ''),
('ATT0462', 'SES012', 'STU016', 'PRESENT', ''),
('ATT0463', 'SES013', 'STU016', 'PRESENT', ''),
('ATT0464', 'SES014', 'STU016', 'PRESENT', ''),
('ATT0465', 'SES015', 'STU016', 'PRESENT', ''),
('ATT0466', 'SES016', 'STU016', 'PRESENT', ''),
('ATT0467', 'SES017', 'STU016', 'PRESENT', ''),
('ATT0468', 'SES018', 'STU016', 'PRESENT', ''),
('ATT0469', 'SES019', 'STU016', 'PRESENT', ''),
('ATT0470', 'SES020', 'STU016', 'PRESENT', ''),
('ATT0471', 'SES021', 'STU016', 'PRESENT', ''),
('ATT0472', 'SES022', 'STU016', 'PRESENT', ''),
('ATT0473', 'SES023', 'STU016', 'PRESENT', ''),
('ATT0474', 'SES024', 'STU016', 'PRESENT', ''),
('ATT0475', 'SES025', 'STU016', 'PRESENT', ''),
('ATT0476', 'SES026', 'STU016', 'PRESENT', ''),
('ATT0477', 'SES027', 'STU016', 'PRESENT', ''),
('ATT0478', 'SES028', 'STU016', 'ABSENT', 'Absent'),
('ATT0479', 'SES029', 'STU016', 'ABSENT', 'Absent'),
('ATT0480', 'SES030', 'STU016', 'ABSENT', 'Absent'),
('ATT0481', 'SES001', 'STU017', 'PRESENT', ''),
('ATT0482', 'SES002', 'STU017', 'PRESENT', ''),
('ATT0483', 'SES003', 'STU017', 'PRESENT', ''),
('ATT0484', 'SES004', 'STU017', 'PRESENT', ''),
('ATT0485', 'SES005', 'STU017', 'PRESENT', ''),
('ATT0486', 'SES006', 'STU017', 'PRESENT', ''),
('ATT0487', 'SES007', 'STU017', 'PRESENT', ''),
('ATT0488', 'SES008', 'STU017', 'PRESENT', ''),
('ATT0489', 'SES009', 'STU017', 'PRESENT', ''),
('ATT0490', 'SES010', 'STU017', 'PRESENT', ''),
('ATT0491', 'SES011', 'STU017', 'PRESENT', ''),
('ATT0492', 'SES012', 'STU017', 'PRESENT', ''),
('ATT0493', 'SES013', 'STU017', 'PRESENT', ''),
('ATT0494', 'SES014', 'STU017', 'PRESENT', ''),
('ATT0495', 'SES015', 'STU017', 'PRESENT', ''),
('ATT0496', 'SES016', 'STU017', 'PRESENT', ''),
('ATT0497', 'SES017', 'STU017', 'PRESENT', ''),
('ATT0498', 'SES018', 'STU017', 'PRESENT', ''),
('ATT0499', 'SES019', 'STU017', 'PRESENT', ''),
('ATT0500', 'SES020', 'STU017', 'PRESENT', ''),
('ATT0501', 'SES021', 'STU017', 'PRESENT', ''),
('ATT0502', 'SES022', 'STU017', 'PRESENT', ''),
('ATT0503', 'SES023', 'STU017', 'PRESENT', ''),
('ATT0504', 'SES024', 'STU017', 'PRESENT', ''),
('ATT0505', 'SES025', 'STU017', 'PRESENT', ''),
('ATT0506', 'SES026', 'STU017', 'PRESENT', ''),
('ATT0507', 'SES027', 'STU017', 'PRESENT', ''),
('ATT0508', 'SES028', 'STU017', 'ABSENT', 'Absent'),
('ATT0509', 'SES029', 'STU017', 'ABSENT', 'Absent'),
('ATT0510', 'SES030', 'STU017', 'ABSENT', 'Absent'),
('ATT0511', 'SES001', 'STU018', 'PRESENT', ''),
('ATT0512', 'SES002', 'STU018', 'PRESENT', ''),
('ATT0513', 'SES003', 'STU018', 'PRESENT', ''),
('ATT0514', 'SES004', 'STU018', 'PRESENT', ''),
('ATT0515', 'SES005', 'STU018', 'PRESENT', ''),
('ATT0516', 'SES006', 'STU018', 'PRESENT', ''),
('ATT0517', 'SES007', 'STU018', 'PRESENT', ''),
('ATT0518', 'SES008', 'STU018', 'PRESENT', ''),
('ATT0519', 'SES009', 'STU018', 'PRESENT', ''),
('ATT0520', 'SES010', 'STU018', 'PRESENT', ''),
('ATT0521', 'SES011', 'STU018', 'PRESENT', ''),
('ATT0522', 'SES012', 'STU018', 'PRESENT', ''),
('ATT0523', 'SES013', 'STU018', 'PRESENT', ''),
('ATT0524', 'SES014', 'STU018', 'PRESENT', ''),
('ATT0525', 'SES015', 'STU018', 'PRESENT', ''),
('ATT0526', 'SES016', 'STU018', 'PRESENT', ''),
('ATT0527', 'SES017', 'STU018', 'PRESENT', ''),
('ATT0528', 'SES018', 'STU018', 'PRESENT', ''),
('ATT0529', 'SES019', 'STU018', 'PRESENT', ''),
('ATT0530', 'SES020', 'STU018', 'PRESENT', ''),
('ATT0531', 'SES021', 'STU018', 'PRESENT', ''),
('ATT0532', 'SES022', 'STU018', 'PRESENT', ''),
('ATT0533', 'SES023', 'STU018', 'PRESENT', ''),
('ATT0534', 'SES024', 'STU018', 'PRESENT', ''),
('ATT0535', 'SES025', 'STU018', 'PRESENT', ''),
('ATT0536', 'SES026', 'STU018', 'PRESENT', ''),
('ATT0537', 'SES027', 'STU018', 'PRESENT', ''),
('ATT0538', 'SES028', 'STU018', 'ABSENT', 'Absent'),
('ATT0539', 'SES029', 'STU018', 'ABSENT', 'Absent'),
('ATT0540', 'SES030', 'STU018', 'ABSENT', 'Absent'),
('ATT0541', 'SES001', 'STU019', 'PRESENT', ''),
('ATT0542', 'SES002', 'STU019', 'PRESENT', ''),
('ATT0543', 'SES003', 'STU019', 'PRESENT', ''),
('ATT0544', 'SES004', 'STU019', 'PRESENT', ''),
('ATT0545', 'SES005', 'STU019', 'PRESENT', ''),
('ATT0546', 'SES006', 'STU019', 'PRESENT', ''),
('ATT0547', 'SES007', 'STU019', 'PRESENT', ''),
('ATT0548', 'SES008', 'STU019', 'PRESENT', ''),
('ATT0549', 'SES009', 'STU019', 'PRESENT', ''),
('ATT0550', 'SES010', 'STU019', 'PRESENT', ''),
('ATT0551', 'SES011', 'STU019', 'PRESENT', ''),
('ATT0552', 'SES012', 'STU019', 'PRESENT', ''),
('ATT0553', 'SES013', 'STU019', 'PRESENT', ''),
('ATT0554', 'SES014', 'STU019', 'PRESENT', ''),
('ATT0555', 'SES015', 'STU019', 'PRESENT', ''),
('ATT0556', 'SES016', 'STU019', 'PRESENT', ''),
('ATT0557', 'SES017', 'STU019', 'PRESENT', ''),
('ATT0558', 'SES018', 'STU019', 'PRESENT', ''),
('ATT0559', 'SES019', 'STU019', 'PRESENT', ''),
('ATT0560', 'SES020', 'STU019', 'PRESENT', ''),
('ATT0561', 'SES021', 'STU019', 'PRESENT', ''),
('ATT0562', 'SES022', 'STU019', 'PRESENT', ''),
('ATT0563', 'SES023', 'STU019', 'PRESENT', ''),
('ATT0564', 'SES024', 'STU019', 'PRESENT', ''),
('ATT0565', 'SES025', 'STU019', 'PRESENT', ''),
('ATT0566', 'SES026', 'STU019', 'PRESENT', ''),
('ATT0567', 'SES027', 'STU019', 'PRESENT', ''),
('ATT0568', 'SES028', 'STU019', 'ABSENT', 'Absent'),
('ATT0569', 'SES029', 'STU019', 'ABSENT', 'Absent'),
('ATT0570', 'SES030', 'STU019', 'ABSENT', 'Absent'),
('ATT0571', 'SES001', 'STU020', 'PRESENT', ''),
('ATT0572', 'SES002', 'STU020', 'PRESENT', ''),
('ATT0573', 'SES003', 'STU020', 'PRESENT', ''),
('ATT0574', 'SES004', 'STU020', 'PRESENT', ''),
('ATT0575', 'SES005', 'STU020', 'PRESENT', ''),
('ATT0576', 'SES006', 'STU020', 'PRESENT', ''),
('ATT0577', 'SES007', 'STU020', 'PRESENT', ''),
('ATT0578', 'SES008', 'STU020', 'PRESENT', ''),
('ATT0579', 'SES009', 'STU020', 'PRESENT', ''),
('ATT0580', 'SES010', 'STU020', 'PRESENT', ''),
('ATT0581', 'SES011', 'STU020', 'PRESENT', ''),
('ATT0582', 'SES012', 'STU020', 'PRESENT', ''),
('ATT0583', 'SES013', 'STU020', 'PRESENT', ''),
('ATT0584', 'SES014', 'STU020', 'PRESENT', ''),
('ATT0585', 'SES015', 'STU020', 'PRESENT', ''),
('ATT0586', 'SES016', 'STU020', 'PRESENT', ''),
('ATT0587', 'SES017', 'STU020', 'PRESENT', ''),
('ATT0588', 'SES018', 'STU020', 'PRESENT', ''),
('ATT0589', 'SES019', 'STU020', 'PRESENT', ''),
('ATT0590', 'SES020', 'STU020', 'PRESENT', ''),
('ATT0591', 'SES021', 'STU020', 'PRESENT', ''),
('ATT0592', 'SES022', 'STU020', 'PRESENT', ''),
('ATT0593', 'SES023', 'STU020', 'PRESENT', ''),
('ATT0594', 'SES024', 'STU020', 'PRESENT', ''),
('ATT0595', 'SES025', 'STU020', 'PRESENT', ''),
('ATT0596', 'SES026', 'STU020', 'PRESENT', ''),
('ATT0597', 'SES027', 'STU020', 'PRESENT', ''),
('ATT0598', 'SES028', 'STU020', 'ABSENT', 'Absent'),
('ATT0599', 'SES029', 'STU020', 'ABSENT', 'Absent'),
('ATT0600', 'SES030', 'STU020', 'ABSENT', 'Absent');

-- --------------------------------------------------------

--
-- Table structure for table `attendance_sessions`
--

CREATE TABLE `attendance_sessions` (
  `session_id` varchar(10) NOT NULL,
  `offering_id` varchar(10) NOT NULL,
  `session_date` date NOT NULL,
  `session_type` enum('THEORY','PRACTICAL') NOT NULL,
  `session_number` int(11) NOT NULL,
  `topic` varchar(200) DEFAULT NULL,
  `conducted_by` varchar(10) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `attendance_sessions`
--

INSERT INTO `attendance_sessions` (`session_id`, `offering_id`, `session_date`, `session_type`, `session_number`, `topic`, `conducted_by`) VALUES
('SES001', 'OFF001', '2026-01-20', 'THEORY', 1, 'Theory session 1', 'TO001'),
('SES002', 'OFF001', '2026-01-27', 'THEORY', 2, 'Theory session 2', 'TO001'),
('SES003', 'OFF001', '2026-02-03', 'THEORY', 3, 'Theory session 3', 'TO001'),
('SES004', 'OFF001', '2026-02-10', 'THEORY', 4, 'Theory session 4', 'TO001'),
('SES005', 'OFF001', '2026-02-17', 'THEORY', 5, 'Theory session 5', 'TO001'),
('SES006', 'OFF001', '2026-02-24', 'THEORY', 6, 'Theory session 6', 'TO001'),
('SES007', 'OFF001', '2026-03-03', 'THEORY', 7, 'Theory session 7', 'TO001'),
('SES008', 'OFF001', '2026-03-10', 'THEORY', 8, 'Theory session 8', 'TO001'),
('SES009', 'OFF001', '2026-03-17', 'THEORY', 9, 'Theory session 9', 'TO001'),
('SES010', 'OFF001', '2026-03-24', 'THEORY', 10, 'Theory session 10', 'TO001'),
('SES011', 'OFF001', '2026-03-31', 'THEORY', 11, 'Theory session 11', 'TO001'),
('SES012', 'OFF001', '2026-04-07', 'THEORY', 12, 'Theory session 12', 'TO001'),
('SES013', 'OFF001', '2026-04-14', 'THEORY', 13, 'Theory session 13', 'TO001'),
('SES014', 'OFF001', '2026-04-21', 'THEORY', 14, 'Theory session 14', 'TO001'),
('SES015', 'OFF001', '2026-04-28', 'THEORY', 15, 'Theory session 15', 'TO001'),
('SES016', 'OFF001', '2026-01-23', 'PRACTICAL', 1, 'Practical session 1', 'TO002'),
('SES017', 'OFF001', '2026-01-30', 'PRACTICAL', 2, 'Practical session 2', 'TO002'),
('SES018', 'OFF001', '2026-02-06', 'PRACTICAL', 3, 'Practical session 3', 'TO002'),
('SES019', 'OFF001', '2026-02-13', 'PRACTICAL', 4, 'Practical session 4', 'TO002'),
('SES020', 'OFF001', '2026-02-20', 'PRACTICAL', 5, 'Practical session 5', 'TO002'),
('SES021', 'OFF001', '2026-02-27', 'PRACTICAL', 6, 'Practical session 6', 'TO002'),
('SES022', 'OFF001', '2026-03-06', 'PRACTICAL', 7, 'Practical session 7', 'TO002'),
('SES023', 'OFF001', '2026-03-13', 'PRACTICAL', 8, 'Practical session 8', 'TO002'),
('SES024', 'OFF001', '2026-03-20', 'PRACTICAL', 9, 'Practical session 9', 'TO002'),
('SES025', 'OFF001', '2026-03-27', 'PRACTICAL', 10, 'Practical session 10', 'TO002'),
('SES026', 'OFF001', '2026-04-03', 'PRACTICAL', 11, 'Practical session 11', 'TO002'),
('SES027', 'OFF001', '2026-04-10', 'PRACTICAL', 12, 'Practical session 12', 'TO002'),
('SES028', 'OFF001', '2026-04-17', 'PRACTICAL', 13, 'Practical session 13', 'TO002'),
('SES029', 'OFF001', '2026-04-24', 'PRACTICAL', 14, 'Practical session 14', 'TO002'),
('SES030', 'OFF001', '2026-05-01', 'PRACTICAL', 15, 'Practical session 15', 'TO002');

-- --------------------------------------------------------

--
-- Table structure for table `courses`
--

CREATE TABLE `courses` (
  `course_id` varchar(10) NOT NULL,
  `course_code` varchar(20) NOT NULL,
  `course_name` varchar(150) NOT NULL,
  `credits` int(11) NOT NULL,
  `description` varchar(500) DEFAULT NULL,
  `department` varchar(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `courses`
--

INSERT INTO `courses` (`course_id`, `course_code`, `course_name`, `credits`, `description`, `department`) VALUES
('CRS001', 'ICT2132', 'Object Oriented Programming', 3, 'Object oriented programming concepts and Java application development.', 'Information and Communication Technology'),
('CRS002', 'ICT2142', 'E-Business', 3, 'E-business concepts, models and digital business applications.', 'Information and Communication Technology'),
('CRS003', 'ICT2123', 'Database Management', 3, 'Database design, SQL, normalization and database applications.', 'Information and Communication Technology'),
('CRS004', 'ICT2152', 'Web Development', 3, 'Client-side and server-side web development concepts.', 'Information and Communication Technology'),
('CRS005', 'ICT2162', 'Data Structures', 3, 'Fundamental data structures and algorithms.', 'Information and Communication Technology');

-- --------------------------------------------------------

--
-- Table structure for table `course_lecturers`
--

CREATE TABLE `course_lecturers` (
  `course_lecturer_id` varchar(10) NOT NULL,
  `offering_id` varchar(10) NOT NULL,
  `lecturer_id` varchar(10) NOT NULL,
  `role` varchar(50) DEFAULT 'LECTURER'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `course_lecturers`
--

INSERT INTO `course_lecturers` (`course_lecturer_id`, `offering_id`, `lecturer_id`, `role`) VALUES
('CL001', 'OFF001', 'LEC001', 'LECTURER'),
('CL002', 'OFF002', 'LEC002', 'LECTURER'),
('CL003', 'OFF003', 'LEC003', 'LECTURER'),
('CL004', 'OFF004', 'LEC004', 'LECTURER'),
('CL005', 'OFF005', 'LEC005', 'LECTURER');

-- --------------------------------------------------------

--
-- Table structure for table `course_materials`
--

CREATE TABLE `course_materials` (
  `material_id` varchar(10) NOT NULL,
  `offering_id` varchar(10) NOT NULL,
  `lecturer_id` varchar(10) NOT NULL,
  `title` varchar(150) NOT NULL,
  `description` varchar(500) DEFAULT NULL,
  `file_name` varchar(255) DEFAULT NULL,
  `file_path` varchar(500) DEFAULT NULL,
  `uploaded_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `course_materials`
--

INSERT INTO `course_materials` (`material_id`, `offering_id`, `lecturer_id`, `title`, `description`, `file_name`, `file_path`, `uploaded_at`) VALUES
('MAT001', 'OFF001', 'LEC001', 'OOP Introduction', 'Introduction to object oriented programming concepts.', 'oop_introduction.pdf', 'materials/oop_introduction.pdf', '2026-10-07 14:51:10'),
('MAT002', 'OFF001', 'LEC001', 'Inheritance Notes', 'Java inheritance and superclass/subclass concepts.', 'inheritance.pdf', 'materials/inheritance.pdf', '2026-10-07 14:51:10'),
('MAT003', 'OFF002', 'LEC002', 'E-Business Basics', 'Introduction to e-business models.', 'ebusiness_basics.pdf', 'materials/ebusiness_basics.pdf', '2026-10-07 14:51:10'),
('MAT004', 'OFF003', 'LEC003', 'SQL Fundamentals', 'SQL commands and relational database concepts.', 'sql_fundamentals.pdf', 'materials/sql_fundamentals.pdf', '2026-10-07 14:51:10'),
('MAT005', 'OFF004', 'LEC004', 'HTML and CSS', 'Basic web development notes.', 'html_css.pdf', 'materials/html_css.pdf', '2026-10-07 14:51:10');

-- --------------------------------------------------------

--
-- Table structure for table `course_offerings`
--

CREATE TABLE `course_offerings` (
  `offering_id` varchar(10) NOT NULL,
  `course_id` varchar(10) NOT NULL,
  `academic_year` varchar(20) NOT NULL,
  `semester` enum('SEMESTER 1','SEMESTER 2') NOT NULL,
  `batch` varchar(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `course_offerings`
--

INSERT INTO `course_offerings` (`offering_id`, `course_id`, `academic_year`, `semester`, `batch`) VALUES
('OFF001', 'CRS001', '2026/2027', 'SEMESTER 1', '2024/2025'),
('OFF002', 'CRS002', '2026/2027', 'SEMESTER 1', '2024/2025'),
('OFF003', 'CRS003', '2026/2027', 'SEMESTER 1', '2024/2025'),
('OFF004', 'CRS004', '2026/2027', 'SEMESTER 2', '2024/2025'),
('OFF005', 'CRS005', '2026/2027', 'SEMESTER 2', '2024/2025');

-- --------------------------------------------------------

--
-- Table structure for table `enrollments`
--

CREATE TABLE `enrollments` (
  `enrollment_id` varchar(10) NOT NULL,
  `student_id` varchar(10) NOT NULL,
  `offering_id` varchar(10) NOT NULL,
  `enrollment_date` date NOT NULL,
  `status` enum('ENROLLED','DROPPED','COMPLETED') DEFAULT 'ENROLLED'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `enrollments`
--

INSERT INTO `enrollments` (`enrollment_id`, `student_id`, `offering_id`, `enrollment_date`, `status`) VALUES
('ENR001', 'STU001', 'OFF001', '2026-01-15', 'ENROLLED'),
('ENR002', 'STU002', 'OFF001', '2026-01-15', 'ENROLLED'),
('ENR003', 'STU003', 'OFF001', '2026-01-15', 'ENROLLED'),
('ENR004', 'STU004', 'OFF001', '2026-01-15', 'ENROLLED'),
('ENR005', 'STU005', 'OFF001', '2026-01-15', 'ENROLLED'),
('ENR006', 'STU006', 'OFF001', '2026-01-15', 'ENROLLED'),
('ENR007', 'STU007', 'OFF001', '2026-01-15', 'ENROLLED'),
('ENR008', 'STU008', 'OFF001', '2026-01-15', 'ENROLLED'),
('ENR009', 'STU009', 'OFF001', '2026-01-15', 'ENROLLED'),
('ENR010', 'STU010', 'OFF001', '2026-01-15', 'ENROLLED'),
('ENR011', 'STU011', 'OFF001', '2026-01-15', 'ENROLLED'),
('ENR012', 'STU012', 'OFF001', '2026-01-15', 'ENROLLED'),
('ENR013', 'STU013', 'OFF001', '2026-01-15', 'ENROLLED'),
('ENR014', 'STU014', 'OFF001', '2026-01-15', 'ENROLLED'),
('ENR015', 'STU015', 'OFF001', '2026-01-15', 'ENROLLED'),
('ENR016', 'STU016', 'OFF001', '2026-01-15', 'ENROLLED'),
('ENR017', 'STU017', 'OFF001', '2026-01-15', 'ENROLLED'),
('ENR018', 'STU018', 'OFF001', '2026-01-15', 'ENROLLED'),
('ENR019', 'STU019', 'OFF001', '2026-01-15', 'ENROLLED'),
('ENR020', 'STU020', 'OFF001', '2026-01-15', 'ENROLLED'),
('ENR021', 'STU001', 'OFF002', '2026-01-15', 'ENROLLED'),
('ENR022', 'STU002', 'OFF002', '2026-01-15', 'ENROLLED'),
('ENR023', 'STU003', 'OFF002', '2026-01-15', 'ENROLLED'),
('ENR024', 'STU004', 'OFF002', '2026-01-15', 'ENROLLED'),
('ENR025', 'STU005', 'OFF002', '2026-01-15', 'ENROLLED'),
('ENR026', 'STU006', 'OFF002', '2026-01-15', 'ENROLLED'),
('ENR027', 'STU007', 'OFF002', '2026-01-15', 'ENROLLED'),
('ENR028', 'STU008', 'OFF002', '2026-01-15', 'ENROLLED'),
('ENR029', 'STU009', 'OFF002', '2026-01-15', 'ENROLLED'),
('ENR030', 'STU010', 'OFF002', '2026-01-15', 'ENROLLED'),
('ENR031', 'STU011', 'OFF002', '2026-01-15', 'ENROLLED'),
('ENR032', 'STU012', 'OFF002', '2026-01-15', 'ENROLLED'),
('ENR033', 'STU013', 'OFF002', '2026-01-15', 'ENROLLED'),
('ENR034', 'STU014', 'OFF002', '2026-01-15', 'ENROLLED'),
('ENR035', 'STU015', 'OFF002', '2026-01-15', 'ENROLLED'),
('ENR036', 'STU016', 'OFF002', '2026-01-15', 'ENROLLED'),
('ENR037', 'STU017', 'OFF002', '2026-01-15', 'ENROLLED'),
('ENR038', 'STU018', 'OFF002', '2026-01-15', 'ENROLLED'),
('ENR039', 'STU019', 'OFF002', '2026-01-15', 'ENROLLED'),
('ENR040', 'STU020', 'OFF002', '2026-01-15', 'ENROLLED'),
('ENR041', 'STU001', 'OFF003', '2026-01-15', 'ENROLLED'),
('ENR042', 'STU002', 'OFF003', '2026-01-15', 'ENROLLED'),
('ENR043', 'STU003', 'OFF003', '2026-01-15', 'ENROLLED'),
('ENR044', 'STU004', 'OFF003', '2026-01-15', 'ENROLLED'),
('ENR045', 'STU005', 'OFF003', '2026-01-15', 'ENROLLED'),
('ENR046', 'STU006', 'OFF003', '2026-01-15', 'ENROLLED'),
('ENR047', 'STU007', 'OFF003', '2026-01-15', 'ENROLLED'),
('ENR048', 'STU008', 'OFF003', '2026-01-15', 'ENROLLED'),
('ENR049', 'STU009', 'OFF003', '2026-01-15', 'ENROLLED'),
('ENR050', 'STU010', 'OFF003', '2026-01-15', 'ENROLLED'),
('ENR051', 'STU011', 'OFF003', '2026-01-15', 'ENROLLED'),
('ENR052', 'STU012', 'OFF003', '2026-01-15', 'ENROLLED'),
('ENR053', 'STU013', 'OFF003', '2026-01-15', 'ENROLLED'),
('ENR054', 'STU014', 'OFF003', '2026-01-15', 'ENROLLED'),
('ENR055', 'STU015', 'OFF003', '2026-01-15', 'ENROLLED'),
('ENR056', 'STU016', 'OFF003', '2026-01-15', 'ENROLLED'),
('ENR057', 'STU017', 'OFF003', '2026-01-15', 'ENROLLED'),
('ENR058', 'STU018', 'OFF003', '2026-01-15', 'ENROLLED'),
('ENR059', 'STU019', 'OFF003', '2026-01-15', 'ENROLLED'),
('ENR060', 'STU020', 'OFF003', '2026-01-15', 'ENROLLED');

-- --------------------------------------------------------

--
-- Table structure for table `lecturers`
--

CREATE TABLE `lecturers` (
  `lecturer_id` varchar(10) NOT NULL,
  `staff_no` varchar(30) NOT NULL,
  `department` varchar(100) NOT NULL,
  `designation` varchar(100) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `lecturers`
--

INSERT INTO `lecturers` (`lecturer_id`, `staff_no`, `department`, `designation`) VALUES
('LEC001', 'LEC-STF-001', 'Information and Communication Technology', 'Senior Lecturer'),
('LEC002', 'LEC-STF-002', 'Information and Communication Technology', 'Lecturer'),
('LEC003', 'LEC-STF-003', 'Information and Communication Technology', 'Lecturer'),
('LEC004', 'LEC-STF-004', 'Information and Communication Technology', 'Lecturer'),
('LEC005', 'LEC-STF-005', 'Information and Communication Technology', 'Lecturer');

-- --------------------------------------------------------

--
-- Table structure for table `marks`
--

CREATE TABLE `marks` (
  `mark_id` varchar(10) NOT NULL,
  `assessment_id` varchar(10) NOT NULL,
  `student_id` varchar(10) NOT NULL,
  `marks` decimal(5,2) NOT NULL,
  `entered_by` varchar(10) DEFAULT NULL,
  `entered_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `marks`
--

INSERT INTO `marks` (`mark_id`, `assessment_id`, `student_id`, `marks`, `entered_by`, `entered_at`) VALUES
('MRK0001', 'ASM001', 'STU001', 65.00, 'LEC001', '2026-10-07 14:51:10'),
('MRK0002', 'ASM002', 'STU001', 70.00, 'LEC001', '2026-10-07 14:51:10'),
('MRK0003', 'ASM003', 'STU001', 62.00, 'LEC001', '2026-10-07 14:51:10'),
('MRK0004', 'ASM004', 'STU001', 65.00, 'LEC001', '2026-10-07 14:51:10'),
('MRK0005', 'ASM001', 'STU002', 68.00, 'LEC001', '2026-10-07 14:51:10'),
('MRK0006', 'ASM002', 'STU002', 72.00, 'LEC001', '2026-10-07 14:51:10'),
('MRK0007', 'ASM003', 'STU002', 66.00, 'LEC001', '2026-10-07 14:51:10'),
('MRK0008', 'ASM004', 'STU002', 70.00, 'LEC001', '2026-10-07 14:51:10'),
('MRK0009', 'ASM001', 'STU003', 71.00, 'LEC001', '2026-10-07 14:51:10'),
('MRK0010', 'ASM002', 'STU003', 74.00, 'LEC001', '2026-10-07 14:51:10'),
('MRK0011', 'ASM003', 'STU003', 70.00, 'LEC001', '2026-10-07 14:51:10'),
('MRK0012', 'ASM004', 'STU003', 75.00, 'LEC001', '2026-10-07 14:51:10'),
('MRK0013', 'ASM001', 'STU004', 74.00, 'LEC001', '2026-10-07 14:51:10'),
('MRK0014', 'ASM002', 'STU004', 76.00, 'LEC001', '2026-10-07 14:51:10'),
('MRK0015', 'ASM003', 'STU004', 74.00, 'LEC001', '2026-10-07 14:51:10'),
('MRK0016', 'ASM004', 'STU004', 80.00, 'LEC001', '2026-10-07 14:51:10'),
('MRK0017', 'ASM001', 'STU005', 77.00, 'LEC001', '2026-10-07 14:51:10'),
('MRK0018', 'ASM002', 'STU005', 78.00, 'LEC001', '2026-10-07 14:51:10'),
('MRK0019', 'ASM003', 'STU005', 78.00, 'LEC001', '2026-10-07 14:51:10'),
('MRK0020', 'ASM004', 'STU005', 85.00, 'LEC001', '2026-10-07 14:51:10'),
('MRK0021', 'ASM001', 'STU006', 80.00, 'LEC001', '2026-10-07 14:51:10'),
('MRK0022', 'ASM002', 'STU006', 80.00, 'LEC001', '2026-10-07 14:51:10'),
('MRK0023', 'ASM003', 'STU006', 82.00, 'LEC001', '2026-10-07 14:51:10'),
('MRK0024', 'ASM004', 'STU006', 90.00, 'LEC001', '2026-10-07 14:51:10'),
('MRK0025', 'ASM001', 'STU007', 83.00, 'LEC001', '2026-10-07 14:51:10'),
('MRK0026', 'ASM002', 'STU007', 82.00, 'LEC001', '2026-10-07 14:51:10'),
('MRK0027', 'ASM003', 'STU007', 86.00, 'LEC001', '2026-10-07 14:51:10'),
('MRK0028', 'ASM004', 'STU007', 60.00, 'LEC001', '2026-10-07 14:51:10'),
('MRK0029', 'ASM001', 'STU008', 86.00, 'LEC001', '2026-10-07 14:51:10'),
('MRK0030', 'ASM002', 'STU008', 84.00, 'LEC001', '2026-10-07 14:51:10'),
('MRK0031', 'ASM003', 'STU008', 90.00, 'LEC001', '2026-10-07 14:51:10'),
('MRK0032', 'ASM004', 'STU008', 65.00, 'LEC001', '2026-10-07 14:51:10'),
('MRK0033', 'ASM001', 'STU009', 89.00, 'LEC001', '2026-10-07 14:51:10'),
('MRK0034', 'ASM002', 'STU009', 86.00, 'LEC001', '2026-10-07 14:51:10'),
('MRK0035', 'ASM003', 'STU009', 58.00, 'LEC001', '2026-10-07 14:51:10'),
('MRK0036', 'ASM004', 'STU009', 70.00, 'LEC001', '2026-10-07 14:51:10'),
('MRK0037', 'ASM001', 'STU010', 92.00, 'LEC001', '2026-10-07 14:51:10'),
('MRK0038', 'ASM002', 'STU010', 88.00, 'LEC001', '2026-10-07 14:51:10'),
('MRK0039', 'ASM003', 'STU010', 62.00, 'LEC001', '2026-10-07 14:51:10'),
('MRK0040', 'ASM004', 'STU010', 75.00, 'LEC001', '2026-10-07 14:51:10'),
('MRK0041', 'ASM001', 'STU011', 64.00, 'LEC001', '2026-10-07 14:51:10'),
('MRK0042', 'ASM002', 'STU011', 90.00, 'LEC001', '2026-10-07 14:51:10'),
('MRK0043', 'ASM003', 'STU011', 66.00, 'LEC001', '2026-10-07 14:51:10'),
('MRK0044', 'ASM004', 'STU011', 80.00, 'LEC001', '2026-10-07 14:51:10'),
('MRK0045', 'ASM001', 'STU012', 67.00, 'LEC001', '2026-10-07 14:51:10'),
('MRK0046', 'ASM002', 'STU012', 92.00, 'LEC001', '2026-10-07 14:51:10'),
('MRK0047', 'ASM003', 'STU012', 70.00, 'LEC001', '2026-10-07 14:51:10'),
('MRK0048', 'ASM004', 'STU012', 85.00, 'LEC001', '2026-10-07 14:51:10'),
('MRK0049', 'ASM001', 'STU013', 70.00, 'LEC001', '2026-10-07 14:51:10'),
('MRK0050', 'ASM002', 'STU013', 94.00, 'LEC001', '2026-10-07 14:51:10'),
('MRK0051', 'ASM003', 'STU013', 74.00, 'LEC001', '2026-10-07 14:51:10'),
('MRK0052', 'ASM004', 'STU013', 90.00, 'LEC001', '2026-10-07 14:51:10'),
('MRK0053', 'ASM001', 'STU014', 73.00, 'LEC001', '2026-10-07 14:51:10'),
('MRK0054', 'ASM002', 'STU014', 68.00, 'LEC001', '2026-10-07 14:51:10'),
('MRK0055', 'ASM003', 'STU014', 78.00, 'LEC001', '2026-10-07 14:51:10'),
('MRK0056', 'ASM004', 'STU014', 60.00, 'LEC001', '2026-10-07 14:51:10'),
('MRK0057', 'ASM001', 'STU015', 76.00, 'LEC001', '2026-10-07 14:51:10'),
('MRK0058', 'ASM002', 'STU015', 70.00, 'LEC001', '2026-10-07 14:51:10'),
('MRK0059', 'ASM003', 'STU015', 82.00, 'LEC001', '2026-10-07 14:51:10'),
('MRK0060', 'ASM004', 'STU015', 65.00, 'LEC001', '2026-10-07 14:51:10'),
('MRK0061', 'ASM001', 'STU016', 79.00, 'LEC001', '2026-10-07 14:51:10'),
('MRK0062', 'ASM002', 'STU016', 72.00, 'LEC001', '2026-10-07 14:51:10'),
('MRK0063', 'ASM003', 'STU016', 86.00, 'LEC001', '2026-10-07 14:51:10'),
('MRK0064', 'ASM004', 'STU016', 70.00, 'LEC001', '2026-10-07 14:51:10'),
('MRK0065', 'ASM001', 'STU017', 82.00, 'LEC001', '2026-10-07 14:51:10'),
('MRK0066', 'ASM002', 'STU017', 74.00, 'LEC001', '2026-10-07 14:51:10'),
('MRK0067', 'ASM003', 'STU017', 90.00, 'LEC001', '2026-10-07 14:51:10'),
('MRK0068', 'ASM004', 'STU017', 75.00, 'LEC001', '2026-10-07 14:51:10'),
('MRK0069', 'ASM001', 'STU018', 85.00, 'LEC001', '2026-10-07 14:51:10'),
('MRK0070', 'ASM002', 'STU018', 76.00, 'LEC001', '2026-10-07 14:51:10'),
('MRK0071', 'ASM003', 'STU018', 58.00, 'LEC001', '2026-10-07 14:51:10'),
('MRK0072', 'ASM004', 'STU018', 80.00, 'LEC001', '2026-10-07 14:51:10'),
('MRK0073', 'ASM001', 'STU019', 88.00, 'LEC001', '2026-10-07 14:51:10'),
('MRK0074', 'ASM002', 'STU019', 78.00, 'LEC001', '2026-10-07 14:51:10'),
('MRK0075', 'ASM003', 'STU019', 62.00, 'LEC001', '2026-10-07 14:51:10'),
('MRK0076', 'ASM004', 'STU019', 85.00, 'LEC001', '2026-10-07 14:51:10'),
('MRK0077', 'ASM001', 'STU020', 91.00, 'LEC001', '2026-10-07 14:51:10'),
('MRK0078', 'ASM002', 'STU020', 80.00, 'LEC001', '2026-10-07 14:51:10'),
('MRK0079', 'ASM003', 'STU020', 66.00, 'LEC001', '2026-10-07 14:51:10'),
('MRK0080', 'ASM004', 'STU020', 90.00, 'LEC001', '2026-10-07 14:51:10'),
('MRK0081', 'ASM005', 'STU001', 69.00, 'LEC002', '2026-10-07 14:51:10'),
('MRK0082', 'ASM006', 'STU001', 75.00, 'LEC002', '2026-10-07 14:51:10'),
('MRK0083', 'ASM007', 'STU001', 65.00, 'LEC002', '2026-10-07 14:51:10'),
('MRK0084', 'ASM008', 'STU001', 67.00, 'LEC002', '2026-10-07 14:51:10'),
('MRK0085', 'ASM005', 'STU002', 72.00, 'LEC002', '2026-10-07 14:51:10'),
('MRK0086', 'ASM006', 'STU002', 77.00, 'LEC002', '2026-10-07 14:51:10'),
('MRK0087', 'ASM007', 'STU002', 69.00, 'LEC002', '2026-10-07 14:51:10'),
('MRK0088', 'ASM008', 'STU002', 72.00, 'LEC002', '2026-10-07 14:51:10'),
('MRK0089', 'ASM005', 'STU003', 75.00, 'LEC002', '2026-10-07 14:51:10'),
('MRK0090', 'ASM006', 'STU003', 79.00, 'LEC002', '2026-10-07 14:51:10'),
('MRK0091', 'ASM007', 'STU003', 73.00, 'LEC002', '2026-10-07 14:51:10'),
('MRK0092', 'ASM008', 'STU003', 77.00, 'LEC002', '2026-10-07 14:51:10'),
('MRK0093', 'ASM005', 'STU004', 78.00, 'LEC002', '2026-10-07 14:51:10'),
('MRK0094', 'ASM006', 'STU004', 81.00, 'LEC002', '2026-10-07 14:51:10'),
('MRK0095', 'ASM007', 'STU004', 77.00, 'LEC002', '2026-10-07 14:51:10'),
('MRK0096', 'ASM008', 'STU004', 82.00, 'LEC002', '2026-10-07 14:51:10'),
('MRK0097', 'ASM005', 'STU005', 81.00, 'LEC002', '2026-10-07 14:51:10'),
('MRK0098', 'ASM006', 'STU005', 83.00, 'LEC002', '2026-10-07 14:51:10'),
('MRK0099', 'ASM007', 'STU005', 81.00, 'LEC002', '2026-10-07 14:51:10'),
('MRK0100', 'ASM008', 'STU005', 87.00, 'LEC002', '2026-10-07 14:51:10'),
('MRK0101', 'ASM005', 'STU006', 84.00, 'LEC002', '2026-10-07 14:51:10'),
('MRK0102', 'ASM006', 'STU006', 85.00, 'LEC002', '2026-10-07 14:51:10'),
('MRK0103', 'ASM007', 'STU006', 85.00, 'LEC002', '2026-10-07 14:51:10'),
('MRK0104', 'ASM008', 'STU006', 92.00, 'LEC002', '2026-10-07 14:51:10'),
('MRK0105', 'ASM005', 'STU007', 87.00, 'LEC002', '2026-10-07 14:51:10'),
('MRK0106', 'ASM006', 'STU007', 87.00, 'LEC002', '2026-10-07 14:51:10'),
('MRK0107', 'ASM007', 'STU007', 89.00, 'LEC002', '2026-10-07 14:51:10'),
('MRK0108', 'ASM008', 'STU007', 62.00, 'LEC002', '2026-10-07 14:51:10'),
('MRK0109', 'ASM005', 'STU008', 90.00, 'LEC002', '2026-10-07 14:51:10'),
('MRK0110', 'ASM006', 'STU008', 89.00, 'LEC002', '2026-10-07 14:51:10'),
('MRK0111', 'ASM007', 'STU008', 93.00, 'LEC002', '2026-10-07 14:51:10'),
('MRK0112', 'ASM008', 'STU008', 67.00, 'LEC002', '2026-10-07 14:51:10'),
('MRK0113', 'ASM005', 'STU009', 62.00, 'LEC002', '2026-10-07 14:51:10'),
('MRK0114', 'ASM006', 'STU009', 91.00, 'LEC002', '2026-10-07 14:51:10'),
('MRK0115', 'ASM007', 'STU009', 61.00, 'LEC002', '2026-10-07 14:51:10'),
('MRK0116', 'ASM008', 'STU009', 72.00, 'LEC002', '2026-10-07 14:51:10'),
('MRK0117', 'ASM005', 'STU010', 65.00, 'LEC002', '2026-10-07 14:51:10'),
('MRK0118', 'ASM006', 'STU010', 93.00, 'LEC002', '2026-10-07 14:51:10'),
('MRK0119', 'ASM007', 'STU010', 65.00, 'LEC002', '2026-10-07 14:51:10'),
('MRK0120', 'ASM008', 'STU010', 77.00, 'LEC002', '2026-10-07 14:51:10'),
('MRK0121', 'ASM005', 'STU011', 68.00, 'LEC002', '2026-10-07 14:51:10'),
('MRK0122', 'ASM006', 'STU011', 95.00, 'LEC002', '2026-10-07 14:51:10'),
('MRK0123', 'ASM007', 'STU011', 69.00, 'LEC002', '2026-10-07 14:51:10'),
('MRK0124', 'ASM008', 'STU011', 82.00, 'LEC002', '2026-10-07 14:51:10'),
('MRK0125', 'ASM005', 'STU012', 71.00, 'LEC002', '2026-10-07 14:51:10'),
('MRK0126', 'ASM006', 'STU012', 69.00, 'LEC002', '2026-10-07 14:51:10'),
('MRK0127', 'ASM007', 'STU012', 73.00, 'LEC002', '2026-10-07 14:51:10'),
('MRK0128', 'ASM008', 'STU012', 87.00, 'LEC002', '2026-10-07 14:51:10'),
('MRK0129', 'ASM005', 'STU013', 74.00, 'LEC002', '2026-10-07 14:51:10'),
('MRK0130', 'ASM006', 'STU013', 71.00, 'LEC002', '2026-10-07 14:51:10'),
('MRK0131', 'ASM007', 'STU013', 77.00, 'LEC002', '2026-10-07 14:51:10'),
('MRK0132', 'ASM008', 'STU013', 92.00, 'LEC002', '2026-10-07 14:51:10'),
('MRK0133', 'ASM005', 'STU014', 77.00, 'LEC002', '2026-10-07 14:51:10'),
('MRK0134', 'ASM006', 'STU014', 73.00, 'LEC002', '2026-10-07 14:51:10'),
('MRK0135', 'ASM007', 'STU014', 81.00, 'LEC002', '2026-10-07 14:51:10'),
('MRK0136', 'ASM008', 'STU014', 62.00, 'LEC002', '2026-10-07 14:51:10'),
('MRK0137', 'ASM005', 'STU015', 80.00, 'LEC002', '2026-10-07 14:51:10'),
('MRK0138', 'ASM006', 'STU015', 75.00, 'LEC002', '2026-10-07 14:51:10'),
('MRK0139', 'ASM007', 'STU015', 85.00, 'LEC002', '2026-10-07 14:51:10'),
('MRK0140', 'ASM008', 'STU015', 67.00, 'LEC002', '2026-10-07 14:51:10'),
('MRK0141', 'ASM005', 'STU016', 83.00, 'LEC002', '2026-10-07 14:51:10'),
('MRK0142', 'ASM006', 'STU016', 77.00, 'LEC002', '2026-10-07 14:51:10'),
('MRK0143', 'ASM007', 'STU016', 89.00, 'LEC002', '2026-10-07 14:51:10'),
('MRK0144', 'ASM008', 'STU016', 72.00, 'LEC002', '2026-10-07 14:51:10'),
('MRK0145', 'ASM005', 'STU017', 86.00, 'LEC002', '2026-10-07 14:51:10'),
('MRK0146', 'ASM006', 'STU017', 79.00, 'LEC002', '2026-10-07 14:51:10'),
('MRK0147', 'ASM007', 'STU017', 93.00, 'LEC002', '2026-10-07 14:51:10'),
('MRK0148', 'ASM008', 'STU017', 77.00, 'LEC002', '2026-10-07 14:51:10'),
('MRK0149', 'ASM005', 'STU018', 89.00, 'LEC002', '2026-10-07 14:51:10'),
('MRK0150', 'ASM006', 'STU018', 81.00, 'LEC002', '2026-10-07 14:51:10'),
('MRK0151', 'ASM007', 'STU018', 61.00, 'LEC002', '2026-10-07 14:51:10'),
('MRK0152', 'ASM008', 'STU018', 82.00, 'LEC002', '2026-10-07 14:51:10'),
('MRK0153', 'ASM005', 'STU019', 92.00, 'LEC002', '2026-10-07 14:51:10'),
('MRK0154', 'ASM006', 'STU019', 83.00, 'LEC002', '2026-10-07 14:51:10'),
('MRK0155', 'ASM007', 'STU019', 65.00, 'LEC002', '2026-10-07 14:51:10'),
('MRK0156', 'ASM008', 'STU019', 87.00, 'LEC002', '2026-10-07 14:51:10'),
('MRK0157', 'ASM005', 'STU020', 64.00, 'LEC002', '2026-10-07 14:51:10'),
('MRK0158', 'ASM006', 'STU020', 85.00, 'LEC002', '2026-10-07 14:51:10'),
('MRK0159', 'ASM007', 'STU020', 69.00, 'LEC002', '2026-10-07 14:51:10'),
('MRK0160', 'ASM008', 'STU020', 92.00, 'LEC002', '2026-10-07 14:51:10'),
('MRK0161', 'ASM009', 'STU001', 73.00, 'LEC003', '2026-10-07 14:51:10'),
('MRK0162', 'ASM010', 'STU001', 80.00, 'LEC003', '2026-10-07 14:51:10'),
('MRK0163', 'ASM011', 'STU001', 68.00, 'LEC003', '2026-10-07 14:51:10'),
('MRK0164', 'ASM012', 'STU001', 69.00, 'LEC003', '2026-10-07 14:51:10'),
('MRK0165', 'ASM009', 'STU002', 76.00, 'LEC003', '2026-10-07 14:51:10'),
('MRK0166', 'ASM010', 'STU002', 82.00, 'LEC003', '2026-10-07 14:51:10'),
('MRK0167', 'ASM011', 'STU002', 72.00, 'LEC003', '2026-10-07 14:51:10'),
('MRK0168', 'ASM012', 'STU002', 74.00, 'LEC003', '2026-10-07 14:51:10'),
('MRK0169', 'ASM009', 'STU003', 79.00, 'LEC003', '2026-10-07 14:51:10'),
('MRK0170', 'ASM010', 'STU003', 84.00, 'LEC003', '2026-10-07 14:51:10'),
('MRK0171', 'ASM011', 'STU003', 76.00, 'LEC003', '2026-10-07 14:51:10'),
('MRK0172', 'ASM012', 'STU003', 79.00, 'LEC003', '2026-10-07 14:51:10'),
('MRK0173', 'ASM009', 'STU004', 82.00, 'LEC003', '2026-10-07 14:51:10'),
('MRK0174', 'ASM010', 'STU004', 86.00, 'LEC003', '2026-10-07 14:51:10'),
('MRK0175', 'ASM011', 'STU004', 80.00, 'LEC003', '2026-10-07 14:51:10'),
('MRK0176', 'ASM012', 'STU004', 84.00, 'LEC003', '2026-10-07 14:51:10'),
('MRK0177', 'ASM009', 'STU005', 85.00, 'LEC003', '2026-10-07 14:51:10'),
('MRK0178', 'ASM010', 'STU005', 88.00, 'LEC003', '2026-10-07 14:51:10'),
('MRK0179', 'ASM011', 'STU005', 84.00, 'LEC003', '2026-10-07 14:51:10'),
('MRK0180', 'ASM012', 'STU005', 89.00, 'LEC003', '2026-10-07 14:51:10'),
('MRK0181', 'ASM009', 'STU006', 88.00, 'LEC003', '2026-10-07 14:51:10'),
('MRK0182', 'ASM010', 'STU006', 90.00, 'LEC003', '2026-10-07 14:51:10'),
('MRK0183', 'ASM011', 'STU006', 88.00, 'LEC003', '2026-10-07 14:51:10'),
('MRK0184', 'ASM012', 'STU006', 94.00, 'LEC003', '2026-10-07 14:51:10'),
('MRK0185', 'ASM009', 'STU007', 91.00, 'LEC003', '2026-10-07 14:51:10'),
('MRK0186', 'ASM010', 'STU007', 92.00, 'LEC003', '2026-10-07 14:51:10'),
('MRK0187', 'ASM011', 'STU007', 92.00, 'LEC003', '2026-10-07 14:51:10'),
('MRK0188', 'ASM012', 'STU007', 64.00, 'LEC003', '2026-10-07 14:51:10'),
('MRK0189', 'ASM009', 'STU008', 63.00, 'LEC003', '2026-10-07 14:51:10'),
('MRK0190', 'ASM010', 'STU008', 94.00, 'LEC003', '2026-10-07 14:51:10'),
('MRK0191', 'ASM011', 'STU008', 60.00, 'LEC003', '2026-10-07 14:51:10'),
('MRK0192', 'ASM012', 'STU008', 69.00, 'LEC003', '2026-10-07 14:51:10'),
('MRK0193', 'ASM009', 'STU009', 66.00, 'LEC003', '2026-10-07 14:51:10'),
('MRK0194', 'ASM010', 'STU009', 68.00, 'LEC003', '2026-10-07 14:51:10'),
('MRK0195', 'ASM011', 'STU009', 64.00, 'LEC003', '2026-10-07 14:51:10'),
('MRK0196', 'ASM012', 'STU009', 74.00, 'LEC003', '2026-10-07 14:51:10'),
('MRK0197', 'ASM009', 'STU010', 69.00, 'LEC003', '2026-10-07 14:51:10'),
('MRK0198', 'ASM010', 'STU010', 70.00, 'LEC003', '2026-10-07 14:51:10'),
('MRK0199', 'ASM011', 'STU010', 68.00, 'LEC003', '2026-10-07 14:51:10'),
('MRK0200', 'ASM012', 'STU010', 79.00, 'LEC003', '2026-10-07 14:51:10'),
('MRK0201', 'ASM009', 'STU011', 72.00, 'LEC003', '2026-10-07 14:51:10'),
('MRK0202', 'ASM010', 'STU011', 72.00, 'LEC003', '2026-10-07 14:51:10'),
('MRK0203', 'ASM011', 'STU011', 72.00, 'LEC003', '2026-10-07 14:51:10'),
('MRK0204', 'ASM012', 'STU011', 84.00, 'LEC003', '2026-10-07 14:51:10'),
('MRK0205', 'ASM009', 'STU012', 75.00, 'LEC003', '2026-10-07 14:51:10'),
('MRK0206', 'ASM010', 'STU012', 74.00, 'LEC003', '2026-10-07 14:51:10'),
('MRK0207', 'ASM011', 'STU012', 76.00, 'LEC003', '2026-10-07 14:51:10'),
('MRK0208', 'ASM012', 'STU012', 89.00, 'LEC003', '2026-10-07 14:51:10'),
('MRK0209', 'ASM009', 'STU013', 78.00, 'LEC003', '2026-10-07 14:51:10'),
('MRK0210', 'ASM010', 'STU013', 76.00, 'LEC003', '2026-10-07 14:51:10'),
('MRK0211', 'ASM011', 'STU013', 80.00, 'LEC003', '2026-10-07 14:51:10'),
('MRK0212', 'ASM012', 'STU013', 94.00, 'LEC003', '2026-10-07 14:51:10'),
('MRK0213', 'ASM009', 'STU014', 81.00, 'LEC003', '2026-10-07 14:51:10'),
('MRK0214', 'ASM010', 'STU014', 78.00, 'LEC003', '2026-10-07 14:51:10'),
('MRK0215', 'ASM011', 'STU014', 84.00, 'LEC003', '2026-10-07 14:51:10'),
('MRK0216', 'ASM012', 'STU014', 64.00, 'LEC003', '2026-10-07 14:51:10'),
('MRK0217', 'ASM009', 'STU015', 84.00, 'LEC003', '2026-10-07 14:51:10'),
('MRK0218', 'ASM010', 'STU015', 80.00, 'LEC003', '2026-10-07 14:51:10'),
('MRK0219', 'ASM011', 'STU015', 88.00, 'LEC003', '2026-10-07 14:51:10'),
('MRK0220', 'ASM012', 'STU015', 69.00, 'LEC003', '2026-10-07 14:51:10'),
('MRK0221', 'ASM009', 'STU016', 87.00, 'LEC003', '2026-10-07 14:51:10'),
('MRK0222', 'ASM010', 'STU016', 82.00, 'LEC003', '2026-10-07 14:51:10'),
('MRK0223', 'ASM011', 'STU016', 92.00, 'LEC003', '2026-10-07 14:51:10'),
('MRK0224', 'ASM012', 'STU016', 74.00, 'LEC003', '2026-10-07 14:51:10'),
('MRK0225', 'ASM009', 'STU017', 90.00, 'LEC003', '2026-10-07 14:51:10'),
('MRK0226', 'ASM010', 'STU017', 84.00, 'LEC003', '2026-10-07 14:51:10'),
('MRK0227', 'ASM011', 'STU017', 60.00, 'LEC003', '2026-10-07 14:51:10'),
('MRK0228', 'ASM012', 'STU017', 79.00, 'LEC003', '2026-10-07 14:51:10'),
('MRK0229', 'ASM009', 'STU018', 62.00, 'LEC003', '2026-10-07 14:51:10'),
('MRK0230', 'ASM010', 'STU018', 86.00, 'LEC003', '2026-10-07 14:51:10'),
('MRK0231', 'ASM011', 'STU018', 64.00, 'LEC003', '2026-10-07 14:51:10'),
('MRK0232', 'ASM012', 'STU018', 84.00, 'LEC003', '2026-10-07 14:51:10'),
('MRK0233', 'ASM009', 'STU019', 65.00, 'LEC003', '2026-10-07 14:51:10'),
('MRK0234', 'ASM010', 'STU019', 88.00, 'LEC003', '2026-10-07 14:51:10'),
('MRK0235', 'ASM011', 'STU019', 68.00, 'LEC003', '2026-10-07 14:51:10'),
('MRK0236', 'ASM012', 'STU019', 89.00, 'LEC003', '2026-10-07 14:51:10'),
('MRK0237', 'ASM009', 'STU020', 68.00, 'LEC003', '2026-10-07 14:51:10'),
('MRK0238', 'ASM010', 'STU020', 90.00, 'LEC003', '2026-10-07 14:51:10'),
('MRK0239', 'ASM011', 'STU020', 72.00, 'LEC003', '2026-10-07 14:51:10'),
('MRK0240', 'ASM012', 'STU020', 94.00, 'LEC003', '2026-10-07 14:51:10');

-- --------------------------------------------------------

--
-- Table structure for table `medicals`
--

CREATE TABLE `medicals` (
  `medical_id` varchar(10) NOT NULL,
  `student_id` varchar(10) NOT NULL,
  `offering_id` varchar(10) NOT NULL,
  `medical_date` date NOT NULL,
  `reason` varchar(500) NOT NULL,
  `document_path` varchar(500) DEFAULT NULL,
  `approval_status` enum('PENDING','APPROVED','NOT_APPROVED') DEFAULT 'PENDING',
  `approved_by` varchar(10) DEFAULT NULL,
  `remarks` varchar(500) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `medicals`
--

INSERT INTO `medicals` (`medical_id`, `student_id`, `offering_id`, `medical_date`, `reason`, `document_path`, `approval_status`, `approved_by`, `remarks`) VALUES
('MED001', 'STU005', 'OFF001', '2026-02-10', 'Fever and medical treatment', 'medical/STU005_MED001.pdf', 'APPROVED', 'TO001', 'Approved after document verification.'),
('MED002', 'STU010', 'OFF001', '2026-03-02', 'Viral infection', 'medical/STU010_MED002.pdf', 'PENDING', NULL, 'Awaiting verification.'),
('MED003', 'STU016', 'OFF002', '2026-03-18', 'Medical appointment', 'medical/STU016_MED003.pdf', 'NOT_APPROVED', 'TO002', 'Supporting document was incomplete.');

-- --------------------------------------------------------

--
-- Table structure for table `notices`
--

CREATE TABLE `notices` (
  `notice_id` varchar(10) NOT NULL,
  `title` varchar(200) NOT NULL,
  `description` text NOT NULL,
  `notice_date` date NOT NULL,
  `created_by` varchar(10) NOT NULL,
  `status` enum('ACTIVE','INACTIVE') DEFAULT 'ACTIVE'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `notices`
--

INSERT INTO `notices` (`notice_id`, `title`, `description`, `notice_date`, `created_by`, `status`) VALUES
('NOT001', 'Mid Semester Examination Notice', 'Mid semester examinations will be conducted according to the published timetable.', '2026-03-01', 'ADM001', 'ACTIVE'),
('NOT002', 'Assignment Submission', 'Students are requested to submit pending assignments before the deadline.', '2026-03-05', 'ADM001', 'ACTIVE'),
('NOT003', 'Academic Registration Notice', 'Please check your registered courses and report any issue to the department.', '2026-01-12', 'ADM001', 'ACTIVE');

-- --------------------------------------------------------

--
-- Table structure for table `results`
--

CREATE TABLE `results` (
  `result_id` varchar(10) NOT NULL,
  `student_id` varchar(10) NOT NULL,
  `offering_id` varchar(10) NOT NULL,
  `ca_mark` decimal(5,2) DEFAULT NULL,
  `final_exam_mark` decimal(5,2) DEFAULT NULL,
  `final_mark` decimal(5,2) DEFAULT NULL,
  `grade` varchar(5) DEFAULT NULL,
  `grade_point` decimal(3,2) DEFAULT NULL,
  `attendance_percentage` decimal(5,2) DEFAULT NULL,
  `ca_eligible` tinyint(1) DEFAULT 0,
  `final_eligible` tinyint(1) DEFAULT 0,
  `sgpa` decimal(4,2) DEFAULT NULL,
  `cgpa` decimal(4,2) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `results`
--

INSERT INTO `results` (`result_id`, `student_id`, `offering_id`, `ca_mark`, `final_exam_mark`, `final_mark`, `grade`, `grade_point`, `attendance_percentage`, `ca_eligible`, `final_eligible`, `sgpa`, `cgpa`) VALUES
('RES0001', 'STU001', 'OFF001', 29.00, 65.00, 64.75, 'B', 3.00, 100.00, 0, 0, NULL, NULL),
('RES0002', 'STU002', 'OFF001', 30.50, 70.00, 69.00, 'B+', 3.50, 90.00, 0, 0, NULL, NULL),
('RES0003', 'STU003', 'OFF001', 32.00, 75.00, 73.25, 'A', 4.00, 80.00, 0, 0, NULL, NULL),
('RES0004', 'STU004', 'OFF001', 33.50, 80.00, 77.50, 'A', 4.00, 70.00, 0, 0, NULL, NULL),
('RES0005', 'STU005', 'OFF001', 35.00, 85.00, 81.75, 'A', 4.00, 90.00, 0, 0, NULL, NULL),
('RES0006', 'STU006', 'OFF001', 36.50, 90.00, 86.00, 'A+', 4.00, 90.00, 0, 0, NULL, NULL),
('RES0007', 'STU007', 'OFF001', 38.00, 60.00, 71.00, 'A', 4.00, 90.00, 0, 0, NULL, NULL),
('RES0008', 'STU008', 'OFF001', 39.50, 65.00, 75.25, 'A', 4.00, 90.00, 0, 0, NULL, NULL),
('RES0009', 'STU009', 'OFF001', 32.00, 70.00, 70.50, 'A', 4.00, 90.00, 0, 0, NULL, NULL),
('RES0010', 'STU010', 'OFF001', 33.50, 75.00, 74.75, 'A', 4.00, 90.00, 0, 0, NULL, NULL),
('RES0011', 'STU011', 'OFF001', 31.90, 80.00, 75.90, 'A', 4.00, 90.00, 0, 0, NULL, NULL),
('RES0012', 'STU012', 'OFF001', 33.40, 85.00, 80.15, 'A', 4.00, 90.00, 0, 0, NULL, NULL),
('RES0013', 'STU013', 'OFF001', 34.90, 90.00, 84.40, 'A', 4.00, 90.00, 0, 0, NULL, NULL),
('RES0014', 'STU014', 'OFF001', 33.60, 60.00, 66.60, 'B+', 3.50, 90.00, 0, 0, NULL, NULL),
('RES0015', 'STU015', 'OFF001', 35.10, 65.00, 70.85, 'A', 4.00, 90.00, 0, 0, NULL, NULL),
('RES0016', 'STU016', 'OFF001', 36.60, 70.00, 75.10, 'A', 4.00, 90.00, 0, 0, NULL, NULL),
('RES0017', 'STU017', 'OFF001', 38.10, 75.00, 79.35, 'A', 4.00, 90.00, 0, 0, NULL, NULL),
('RES0018', 'STU018', 'OFF001', 30.60, 80.00, 74.60, 'A', 4.00, 90.00, 0, 0, NULL, NULL),
('RES0019', 'STU019', 'OFF001', 32.10, 85.00, 78.85, 'A', 4.00, 90.00, 0, 0, NULL, NULL),
('RES0020', 'STU020', 'OFF001', 33.60, 90.00, 83.10, 'A', 4.00, 90.00, 0, 0, NULL, NULL),
('RES0021', 'STU001', 'OFF002', 30.65, 67.00, 67.50, 'B+', 3.50, 100.00, 0, 0, NULL, NULL),
('RES0022', 'STU002', 'OFF002', 32.15, 72.00, 71.75, 'A', 4.00, 90.00, 0, 0, NULL, NULL),
('RES0023', 'STU003', 'OFF002', 33.65, 77.00, 76.00, 'A', 4.00, 80.00, 0, 0, NULL, NULL),
('RES0024', 'STU004', 'OFF002', 35.15, 82.00, 80.25, 'A', 4.00, 70.00, 0, 0, NULL, NULL),
('RES0025', 'STU005', 'OFF002', 36.65, 87.00, 84.50, 'A', 4.00, 90.00, 0, 0, NULL, NULL),
('RES0026', 'STU006', 'OFF002', 38.15, 92.00, 88.75, 'A+', 4.00, 90.00, 0, 0, NULL, NULL),
('RES0027', 'STU007', 'OFF002', 39.65, 62.00, 73.75, 'A', 4.00, 90.00, 0, 0, NULL, NULL),
('RES0028', 'STU008', 'OFF002', 41.15, 67.00, 78.00, 'A', 4.00, 90.00, 1, 1, NULL, NULL),
('RES0029', 'STU009', 'OFF002', 30.55, 72.00, 70.15, 'A', 4.00, 90.00, 0, 0, NULL, NULL),
('RES0030', 'STU010', 'OFF002', 32.05, 77.00, 74.40, 'A', 4.00, 90.00, 0, 0, NULL, NULL),
('RES0031', 'STU011', 'OFF002', 33.55, 82.00, 78.65, 'A', 4.00, 90.00, 0, 0, NULL, NULL),
('RES0032', 'STU012', 'OFF002', 32.25, 87.00, 80.10, 'A', 4.00, 90.00, 0, 0, NULL, NULL),
('RES0033', 'STU013', 'OFF002', 33.75, 92.00, 84.35, 'A', 4.00, 90.00, 0, 0, NULL, NULL),
('RES0034', 'STU014', 'OFF002', 35.25, 62.00, 69.35, 'B+', 3.50, 90.00, 0, 0, NULL, NULL),
('RES0035', 'STU015', 'OFF002', 36.75, 67.00, 73.60, 'A', 4.00, 90.00, 0, 0, NULL, NULL),
('RES0036', 'STU016', 'OFF002', 38.25, 72.00, 77.85, 'A', 4.00, 90.00, 0, 0, NULL, NULL),
('RES0037', 'STU017', 'OFF002', 39.75, 77.00, 82.10, 'A', 4.00, 90.00, 0, 0, NULL, NULL),
('RES0038', 'STU018', 'OFF002', 32.25, 82.00, 77.35, 'A', 4.00, 90.00, 0, 0, NULL, NULL),
('RES0039', 'STU019', 'OFF002', 33.75, 87.00, 81.60, 'A', 4.00, 90.00, 0, 0, NULL, NULL),
('RES0040', 'STU020', 'OFF002', 32.15, 92.00, 82.75, 'A', 4.00, 90.00, 0, 0, NULL, NULL),
('RES0041', 'STU001', 'OFF003', 32.30, 69.00, 70.25, 'A', 4.00, 100.00, 0, 0, NULL, NULL),
('RES0042', 'STU002', 'OFF003', 33.80, 74.00, 74.50, 'A', 4.00, 90.00, 0, 0, NULL, NULL),
('RES0043', 'STU003', 'OFF003', 35.30, 79.00, 78.75, 'A', 4.00, 80.00, 0, 0, NULL, NULL),
('RES0044', 'STU004', 'OFF003', 36.80, 84.00, 83.00, 'A', 4.00, 70.00, 0, 0, NULL, NULL),
('RES0045', 'STU005', 'OFF003', 38.30, 89.00, 87.25, 'A+', 4.00, 90.00, 0, 0, NULL, NULL),
('RES0046', 'STU006', 'OFF003', 39.80, 94.00, 91.50, 'A+', 4.00, 90.00, 0, 0, NULL, NULL),
('RES0047', 'STU007', 'OFF003', 41.30, 64.00, 76.50, 'A', 4.00, 90.00, 1, 1, NULL, NULL),
('RES0048', 'STU008', 'OFF003', 30.70, 69.00, 68.65, 'B+', 3.50, 90.00, 0, 0, NULL, NULL),
('RES0049', 'STU009', 'OFF003', 29.40, 74.00, 70.10, 'A', 4.00, 90.00, 0, 0, NULL, NULL),
('RES0050', 'STU010', 'OFF003', 30.90, 79.00, 74.35, 'A', 4.00, 90.00, 0, 0, NULL, NULL),
('RES0051', 'STU011', 'OFF003', 32.40, 84.00, 78.60, 'A', 4.00, 90.00, 0, 0, NULL, NULL),
('RES0052', 'STU012', 'OFF003', 33.90, 89.00, 82.85, 'A', 4.00, 90.00, 0, 0, NULL, NULL),
('RES0053', 'STU013', 'OFF003', 35.40, 94.00, 87.10, 'A+', 4.00, 90.00, 0, 0, NULL, NULL),
('RES0054', 'STU014', 'OFF003', 36.90, 64.00, 72.10, 'A', 4.00, 90.00, 0, 0, NULL, NULL),
('RES0055', 'STU015', 'OFF003', 38.40, 69.00, 76.35, 'A', 4.00, 90.00, 0, 0, NULL, NULL),
('RES0056', 'STU016', 'OFF003', 39.90, 74.00, 80.60, 'A', 4.00, 90.00, 0, 0, NULL, NULL),
('RES0057', 'STU017', 'OFF003', 32.40, 79.00, 75.85, 'A', 4.00, 90.00, 0, 0, NULL, NULL),
('RES0058', 'STU018', 'OFF003', 30.80, 84.00, 77.00, 'A', 4.00, 90.00, 0, 0, NULL, NULL),
('RES0059', 'STU019', 'OFF003', 32.30, 89.00, 81.25, 'A', 4.00, 90.00, 0, 0, NULL, NULL),
('RES0060', 'STU020', 'OFF003', 33.80, 94.00, 85.50, 'A+', 4.00, 90.00, 0, 0, NULL, NULL);

-- --------------------------------------------------------

--
-- Table structure for table `technical_officers`
--

CREATE TABLE `technical_officers` (
  `technical_officer_id` varchar(10) NOT NULL,
  `staff_no` varchar(30) NOT NULL,
  `department` varchar(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `technical_officers`
--

INSERT INTO `technical_officers` (`technical_officer_id`, `staff_no`, `department`) VALUES
('TO001', 'TO-STF-001', 'Information and Communication Technology'),
('TO002', 'TO-STF-002', 'Information and Communication Technology'),
('TO003', 'TO-STF-003', 'Information and Communication Technology'),
('TO004', 'TO-STF-004', 'Information and Communication Technology');

-- --------------------------------------------------------

--
-- Table structure for table `timetables`
--

CREATE TABLE `timetables` (
  `timetable_id` varchar(10) NOT NULL,
  `offering_id` varchar(10) NOT NULL,
  `day_of_week` enum('MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY') NOT NULL,
  `start_time` time NOT NULL,
  `end_time` time NOT NULL,
  `venue` varchar(100) NOT NULL,
  `batch` varchar(20) NOT NULL,
  `department` varchar(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `timetables`
--

INSERT INTO `timetables` (`timetable_id`, `offering_id`, `day_of_week`, `start_time`, `end_time`, `venue`, `batch`, `department`) VALUES
('TBL001', 'OFF001', 'MONDAY', '08:30:00', '10:30:00', 'ICT Lab 01', '2024/2025', 'Information and Communication Technology'),
('TBL002', 'OFF001', 'WEDNESDAY', '13:00:00', '15:00:00', 'Lecture Hall 02', '2024/2025', 'Information and Communication Technology'),
('TBL003', 'OFF002', 'TUESDAY', '08:30:00', '10:30:00', 'Lecture Hall 03', '2024/2025', 'Information and Communication Technology'),
('TBL004', 'OFF003', 'THURSDAY', '10:30:00', '12:30:00', 'Lecture Hall 01', '2024/2025', 'Information and Communication Technology'),
('TBL005', 'OFF004', 'FRIDAY', '08:30:00', '10:30:00', 'ICT Lab 02', '2024/2025', 'Information and Communication Technology'),
('TBL006', 'OFF005', 'SATURDAY', '09:00:00', '11:00:00', 'ICT Lab 03', '2024/2025', 'Information and Communication Technology');

-- --------------------------------------------------------

--
-- Table structure for table `undergraduates`
--

CREATE TABLE `undergraduates` (
  `student_id` varchar(10) NOT NULL,
  `index_no` varchar(30) NOT NULL,
  `batch` varchar(20) NOT NULL,
  `academic_year` varchar(20) NOT NULL,
  `department` varchar(100) NOT NULL,
  `student_status` enum('REGULAR','REPEAT','BATCH_MISSED') DEFAULT 'REGULAR'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `undergraduates`
--

INSERT INTO `undergraduates` (`student_id`, `index_no`, `batch`, `academic_year`, `department`, `student_status`) VALUES
('STU001', 'TG/2024/2001', '2024/2025', 'Level II', 'Information and Communication Technology', 'REGULAR'),
('STU002', 'TG/2024/2002', '2024/2025', 'Level II', 'Information and Communication Technology', 'REGULAR'),
('STU003', 'TG/2024/2003', '2024/2025', 'Level II', 'Information and Communication Technology', 'REGULAR'),
('STU004', 'TG/2024/2004', '2024/2025', 'Level II', 'Information and Communication Technology', 'REGULAR'),
('STU005', 'TG/2024/2005', '2024/2025', 'Level II', 'Information and Communication Technology', 'REGULAR'),
('STU006', 'TG/2024/2006', '2024/2025', 'Level II', 'Information and Communication Technology', 'REGULAR'),
('STU007', 'TG/2024/2007', '2024/2025', 'Level II', 'Information and Communication Technology', 'REGULAR'),
('STU008', 'TG/2024/2008', '2024/2025', 'Level II', 'Information and Communication Technology', 'REGULAR'),
('STU009', 'TG/2024/2009', '2024/2025', 'Level II', 'Information and Communication Technology', 'REGULAR'),
('STU010', 'TG/2024/2010', '2024/2025', 'Level II', 'Information and Communication Technology', 'REGULAR'),
('STU011', 'TG/2024/2011', '2024/2025', 'Level II', 'Information and Communication Technology', 'REGULAR'),
('STU012', 'TG/2024/2012', '2024/2025', 'Level II', 'Information and Communication Technology', 'REGULAR'),
('STU013', 'TG/2024/2013', '2024/2025', 'Level II', 'Information and Communication Technology', 'REGULAR'),
('STU014', 'TG/2024/2014', '2024/2025', 'Level II', 'Information and Communication Technology', 'REGULAR'),
('STU015', 'TG/2024/2015', '2024/2025', 'Level II', 'Information and Communication Technology', 'REGULAR'),
('STU016', 'TG/2024/2016', '2024/2025', 'Level II', 'Information and Communication Technology', 'REPEAT'),
('STU017', 'TG/2024/2017', '2024/2025', 'Level II', 'Information and Communication Technology', 'BATCH_MISSED'),
('STU018', 'TG/2024/2018', '2024/2025', 'Level II', 'Information and Communication Technology', 'REGULAR'),
('STU019', 'TG/2024/2019', '2024/2025', 'Level II', 'Information and Communication Technology', 'REPEAT'),
('STU020', 'TG/2024/2020', '2024/2025', 'Level II', 'Information and Communication Technology', 'BATCH_MISSED');

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `user_id` varchar(10) NOT NULL,
  `username` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `role` enum('ADMIN','LECTURER','TECHNICAL_OFFICER','UNDERGRADUATE') NOT NULL,
  `first_name` varchar(50) NOT NULL,
  `last_name` varchar(50) NOT NULL,
  `email` varchar(100) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL,
  `profile_picture` varchar(255) DEFAULT NULL,
  `status` enum('ACTIVE','INACTIVE') DEFAULT 'ACTIVE',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`user_id`, `username`, `password`, `role`, `first_name`, `last_name`, `email`, `phone`, `address`, `profile_picture`, `status`, `created_at`) VALUES
('ADM001', 'admin001', 'Admin@123', 'ADMIN', 'System', 'Administrator', 'admin@fams.uor.lk', '0710000001', 'Faculty of Technology, University of Ruhuna', NULL, 'ACTIVE', '2026-10-07 14:51:10'),
('LEC001', 'lec001', 'Lec@123', 'LECTURER', 'Kasun', 'Perera', 'kasun.perera@uor.lk', '0710000002', 'Galle', NULL, 'ACTIVE', '2026-10-07 14:51:10'),
('LEC002', 'lec002', 'Lec@123', 'LECTURER', 'Nadeesha', 'Fernando', 'nadeesha.fernando@uor.lk', '0710000003', 'Matara', NULL, 'ACTIVE', '2026-10-07 14:51:10'),
('LEC003', 'lec003', 'Lec@123', 'LECTURER', 'Tharindu', 'Silva', 'tharindu.silva@uor.lk', '0710000004', 'Galle', NULL, 'ACTIVE', '2026-10-07 14:51:10'),
('LEC004', 'lec004', 'Lec@123', 'LECTURER', 'Ayesha', 'Bandara', 'ayesha.bandara@uor.lk', '0710000005', 'Matara', NULL, 'ACTIVE', '2026-10-07 14:51:10'),
('LEC005', 'lec005', 'Lec@123', 'LECTURER', 'Ruwan', 'Jayasinghe', 'ruwan.jayasinghe@uor.lk', '0710000006', 'Hambantota', NULL, 'ACTIVE', '2026-10-07 14:51:10'),
('STU001', 'stu001', 'Stu@123', 'UNDERGRADUATE', 'Amal', 'Perera', 'amal.perera@student.uor.lk', '0711000001', 'Galle', NULL, 'ACTIVE', '2026-10-07 14:51:10'),
('STU002', 'stu002', 'Stu@123', 'UNDERGRADUATE', 'Nimal', 'Fernando', 'nimal.fernando@student.uor.lk', '0711000002', 'Matara', NULL, 'ACTIVE', '2026-10-07 14:51:10'),
('STU003', 'stu003', 'Stu@123', 'UNDERGRADUATE', 'Kasun', 'Silva', 'kasun.silva@student.uor.lk', '0711000003', 'Galle', NULL, 'ACTIVE', '2026-10-07 14:51:10'),
('STU004', 'stu004', 'Stu@123', 'UNDERGRADUATE', 'Tharushi', 'Perera', 'tharushi.perera@student.uor.lk', '0711000004', 'Matara', NULL, 'ACTIVE', '2026-10-07 14:51:10'),
('STU005', 'stu005', 'Stu@123', 'UNDERGRADUATE', 'Sanduni', 'Fernando', 'sanduni.fernando@student.uor.lk', '0711000005', 'Galle', NULL, 'ACTIVE', '2026-10-07 14:51:10'),
('STU006', 'stu006', 'Stu@123', 'UNDERGRADUATE', 'Pasindu', 'Kumara', 'pasindu.kumara@student.uor.lk', '0711000006', 'Matara', NULL, 'ACTIVE', '2026-10-07 14:51:10'),
('STU007', 'stu007', 'Stu@123', 'UNDERGRADUATE', 'Hiruni', 'Silva', 'hiruni.silva@student.uor.lk', '0711000007', 'Galle', NULL, 'ACTIVE', '2026-10-07 14:51:10'),
('STU008', 'stu008', 'Stu@123', 'UNDERGRADUATE', 'Sahan', 'Perera', 'sahan.perera@student.uor.lk', '0711000008', 'Matara', NULL, 'ACTIVE', '2026-10-07 14:51:10'),
('STU009', 'stu009', 'Stu@123', 'UNDERGRADUATE', 'Dinuka', 'Fernando', 'dinuka.fernando@student.uor.lk', '0711000009', 'Galle', NULL, 'ACTIVE', '2026-10-07 14:51:10'),
('STU010', 'stu010', 'Stu@123', 'UNDERGRADUATE', 'Piumi', 'Kumari', 'piumi.kumari@student.uor.lk', '0711000010', 'Matara', NULL, 'ACTIVE', '2026-10-07 14:51:10'),
('STU011', 'stu011', 'Stu@123', 'UNDERGRADUATE', 'Rashmi', 'Silva', 'rashmi.silva@student.uor.lk', '0711000011', 'Galle', NULL, 'ACTIVE', '2026-10-07 14:51:10'),
('STU012', 'stu012', 'Stu@123', 'UNDERGRADUATE', 'Chamara', 'Perera', 'chamara.perera@student.uor.lk', '0711000012', 'Matara', NULL, 'ACTIVE', '2026-10-07 14:51:10'),
('STU013', 'stu013', 'Stu@123', 'UNDERGRADUATE', 'Shalini', 'Fernando', 'shalini.fernando@student.uor.lk', '0711000013', 'Galle', NULL, 'ACTIVE', '2026-10-07 14:51:10'),
('STU014', 'stu014', 'Stu@123', 'UNDERGRADUATE', 'Dulshan', 'Silva', 'dulshan.silva@student.uor.lk', '0711000014', 'Matara', NULL, 'ACTIVE', '2026-10-07 14:51:10'),
('STU015', 'stu015', 'Stu@123', 'UNDERGRADUATE', 'Sachini', 'Perera', 'sachini.perera@student.uor.lk', '0711000015', 'Galle', NULL, 'ACTIVE', '2026-10-07 14:51:10'),
('STU016', 'stu016', 'Stu@123', 'UNDERGRADUATE', 'Isuru', 'Fernando', 'isuru.fernando@student.uor.lk', '0711000016', 'Matara', NULL, 'ACTIVE', '2026-10-07 14:51:10'),
('STU017', 'stu017', 'Stu@123', 'UNDERGRADUATE', 'Madhavi', 'Kumari', 'madhavi.kumari@student.uor.lk', '0711000017', 'Galle', NULL, 'ACTIVE', '2026-10-07 14:51:10'),
('STU018', 'stu018', 'Stu@123', 'UNDERGRADUATE', 'Lahiru', 'Silva', 'lahiru.silva@student.uor.lk', '0711000018', 'Matara', NULL, 'ACTIVE', '2026-10-07 14:51:10'),
('STU019', 'stu019', 'Stu@123', 'UNDERGRADUATE', 'Nethmi', 'Perera', 'nethmi.perera@student.uor.lk', '0711000019', 'Galle', NULL, 'ACTIVE', '2026-10-07 14:51:10'),
('STU020', 'stu020', 'Stu@123', 'UNDERGRADUATE', 'Yasiru', 'Fernando', 'yasiru.fernando@student.uor.lk', '0711000020', 'Matara', NULL, 'ACTIVE', '2026-10-07 14:51:10'),
('TO001', 'to001', 'TO@123', 'TECHNICAL_OFFICER', 'Dinesh', 'Kumara', 'dinesh.to@uor.lk', '0710000007', 'Galle', NULL, 'ACTIVE', '2026-10-07 14:51:10'),
('TO002', 'to002', 'TO@123', 'TECHNICAL_OFFICER', 'Nimali', 'Perera', 'nimali.to@uor.lk', '0710000008', 'Matara', NULL, 'ACTIVE', '2026-10-07 14:51:10'),
('TO003', 'to003', 'TO@123', 'TECHNICAL_OFFICER', 'Sahan', 'Fernando', 'sahan.to@uor.lk', '0710000009', 'Galle', NULL, 'ACTIVE', '2026-10-07 14:51:10'),
('TO004', 'to004', 'TO@123', 'TECHNICAL_OFFICER', 'Dilani', 'Silva', 'dilani.to@uor.lk', '0710000010', 'Matara', NULL, 'ACTIVE', '2026-10-07 14:51:10');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `admins`
--
ALTER TABLE `admins`
  ADD PRIMARY KEY (`admin_id`);

--
-- Indexes for table `assessments`
--
ALTER TABLE `assessments`
  ADD PRIMARY KEY (`assessment_id`),
  ADD KEY `offering_id` (`offering_id`),
  ADD KEY `created_by` (`created_by`);

--
-- Indexes for table `attendance`
--
ALTER TABLE `attendance`
  ADD PRIMARY KEY (`attendance_id`),
  ADD UNIQUE KEY `session_id` (`session_id`,`student_id`),
  ADD KEY `student_id` (`student_id`);

--
-- Indexes for table `attendance_sessions`
--
ALTER TABLE `attendance_sessions`
  ADD PRIMARY KEY (`session_id`),
  ADD UNIQUE KEY `offering_id` (`offering_id`,`session_type`,`session_number`),
  ADD KEY `conducted_by` (`conducted_by`);

--
-- Indexes for table `courses`
--
ALTER TABLE `courses`
  ADD PRIMARY KEY (`course_id`),
  ADD UNIQUE KEY `course_code` (`course_code`);

--
-- Indexes for table `course_lecturers`
--
ALTER TABLE `course_lecturers`
  ADD PRIMARY KEY (`course_lecturer_id`),
  ADD UNIQUE KEY `offering_id` (`offering_id`,`lecturer_id`),
  ADD KEY `lecturer_id` (`lecturer_id`);

--
-- Indexes for table `course_materials`
--
ALTER TABLE `course_materials`
  ADD PRIMARY KEY (`material_id`),
  ADD KEY `offering_id` (`offering_id`),
  ADD KEY `lecturer_id` (`lecturer_id`);

--
-- Indexes for table `course_offerings`
--
ALTER TABLE `course_offerings`
  ADD PRIMARY KEY (`offering_id`),
  ADD UNIQUE KEY `course_id` (`course_id`,`academic_year`,`semester`,`batch`);

--
-- Indexes for table `enrollments`
--
ALTER TABLE `enrollments`
  ADD PRIMARY KEY (`enrollment_id`),
  ADD UNIQUE KEY `student_id` (`student_id`,`offering_id`),
  ADD KEY `offering_id` (`offering_id`);

--
-- Indexes for table `lecturers`
--
ALTER TABLE `lecturers`
  ADD PRIMARY KEY (`lecturer_id`),
  ADD UNIQUE KEY `staff_no` (`staff_no`);

--
-- Indexes for table `marks`
--
ALTER TABLE `marks`
  ADD PRIMARY KEY (`mark_id`),
  ADD UNIQUE KEY `assessment_id` (`assessment_id`,`student_id`),
  ADD KEY `student_id` (`student_id`),
  ADD KEY `entered_by` (`entered_by`);

--
-- Indexes for table `medicals`
--
ALTER TABLE `medicals`
  ADD PRIMARY KEY (`medical_id`),
  ADD KEY `student_id` (`student_id`),
  ADD KEY `offering_id` (`offering_id`),
  ADD KEY `approved_by` (`approved_by`);

--
-- Indexes for table `notices`
--
ALTER TABLE `notices`
  ADD PRIMARY KEY (`notice_id`),
  ADD KEY `created_by` (`created_by`);

--
-- Indexes for table `results`
--
ALTER TABLE `results`
  ADD PRIMARY KEY (`result_id`),
  ADD UNIQUE KEY `student_id` (`student_id`,`offering_id`),
  ADD KEY `offering_id` (`offering_id`);

--
-- Indexes for table `technical_officers`
--
ALTER TABLE `technical_officers`
  ADD PRIMARY KEY (`technical_officer_id`),
  ADD UNIQUE KEY `staff_no` (`staff_no`);

--
-- Indexes for table `timetables`
--
ALTER TABLE `timetables`
  ADD PRIMARY KEY (`timetable_id`),
  ADD KEY `offering_id` (`offering_id`);

--
-- Indexes for table `undergraduates`
--
ALTER TABLE `undergraduates`
  ADD PRIMARY KEY (`student_id`),
  ADD UNIQUE KEY `index_no` (`index_no`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`user_id`),
  ADD UNIQUE KEY `username` (`username`),
  ADD UNIQUE KEY `email` (`email`);

--
-- Constraints for dumped tables
--

--
-- Constraints for table `admins`
--
ALTER TABLE `admins`
  ADD CONSTRAINT `admins_ibfk_1` FOREIGN KEY (`admin_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `assessments`
--
ALTER TABLE `assessments`
  ADD CONSTRAINT `assessments_ibfk_1` FOREIGN KEY (`offering_id`) REFERENCES `course_offerings` (`offering_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `assessments_ibfk_2` FOREIGN KEY (`created_by`) REFERENCES `lecturers` (`lecturer_id`) ON DELETE SET NULL ON UPDATE CASCADE;

--
-- Constraints for table `attendance`
--
ALTER TABLE `attendance`
  ADD CONSTRAINT `attendance_ibfk_1` FOREIGN KEY (`session_id`) REFERENCES `attendance_sessions` (`session_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `attendance_ibfk_2` FOREIGN KEY (`student_id`) REFERENCES `undergraduates` (`student_id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `attendance_sessions`
--
ALTER TABLE `attendance_sessions`
  ADD CONSTRAINT `attendance_sessions_ibfk_1` FOREIGN KEY (`offering_id`) REFERENCES `course_offerings` (`offering_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `attendance_sessions_ibfk_2` FOREIGN KEY (`conducted_by`) REFERENCES `technical_officers` (`technical_officer_id`) ON DELETE SET NULL ON UPDATE CASCADE;

--
-- Constraints for table `course_lecturers`
--
ALTER TABLE `course_lecturers`
  ADD CONSTRAINT `course_lecturers_ibfk_1` FOREIGN KEY (`offering_id`) REFERENCES `course_offerings` (`offering_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `course_lecturers_ibfk_2` FOREIGN KEY (`lecturer_id`) REFERENCES `lecturers` (`lecturer_id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `course_materials`
--
ALTER TABLE `course_materials`
  ADD CONSTRAINT `course_materials_ibfk_1` FOREIGN KEY (`offering_id`) REFERENCES `course_offerings` (`offering_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `course_materials_ibfk_2` FOREIGN KEY (`lecturer_id`) REFERENCES `lecturers` (`lecturer_id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `course_offerings`
--
ALTER TABLE `course_offerings`
  ADD CONSTRAINT `course_offerings_ibfk_1` FOREIGN KEY (`course_id`) REFERENCES `courses` (`course_id`) ON UPDATE CASCADE;

--
-- Constraints for table `enrollments`
--
ALTER TABLE `enrollments`
  ADD CONSTRAINT `enrollments_ibfk_1` FOREIGN KEY (`student_id`) REFERENCES `undergraduates` (`student_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `enrollments_ibfk_2` FOREIGN KEY (`offering_id`) REFERENCES `course_offerings` (`offering_id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `lecturers`
--
ALTER TABLE `lecturers`
  ADD CONSTRAINT `lecturers_ibfk_1` FOREIGN KEY (`lecturer_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `marks`
--
ALTER TABLE `marks`
  ADD CONSTRAINT `marks_ibfk_1` FOREIGN KEY (`assessment_id`) REFERENCES `assessments` (`assessment_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `marks_ibfk_2` FOREIGN KEY (`student_id`) REFERENCES `undergraduates` (`student_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `marks_ibfk_3` FOREIGN KEY (`entered_by`) REFERENCES `lecturers` (`lecturer_id`) ON DELETE SET NULL ON UPDATE CASCADE;

--
-- Constraints for table `medicals`
--
ALTER TABLE `medicals`
  ADD CONSTRAINT `medicals_ibfk_1` FOREIGN KEY (`student_id`) REFERENCES `undergraduates` (`student_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `medicals_ibfk_2` FOREIGN KEY (`offering_id`) REFERENCES `course_offerings` (`offering_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `medicals_ibfk_3` FOREIGN KEY (`approved_by`) REFERENCES `technical_officers` (`technical_officer_id`) ON DELETE SET NULL ON UPDATE CASCADE;

--
-- Constraints for table `notices`
--
ALTER TABLE `notices`
  ADD CONSTRAINT `notices_ibfk_1` FOREIGN KEY (`created_by`) REFERENCES `admins` (`admin_id`) ON UPDATE CASCADE;

--
-- Constraints for table `results`
--
ALTER TABLE `results`
  ADD CONSTRAINT `results_ibfk_1` FOREIGN KEY (`student_id`) REFERENCES `undergraduates` (`student_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `results_ibfk_2` FOREIGN KEY (`offering_id`) REFERENCES `course_offerings` (`offering_id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `technical_officers`
--
ALTER TABLE `technical_officers`
  ADD CONSTRAINT `technical_officers_ibfk_1` FOREIGN KEY (`technical_officer_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `timetables`
--
ALTER TABLE `timetables`
  ADD CONSTRAINT `timetables_ibfk_1` FOREIGN KEY (`offering_id`) REFERENCES `course_offerings` (`offering_id`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `undergraduates`
--
ALTER TABLE `undergraduates`
  ADD CONSTRAINT `undergraduates_ibfk_1` FOREIGN KEY (`student_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;

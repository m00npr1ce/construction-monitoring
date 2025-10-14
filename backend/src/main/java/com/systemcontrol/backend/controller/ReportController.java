package com.systemcontrol.backend.controller;

import com.systemcontrol.backend.model.Defect;
import com.systemcontrol.backend.model.DefectStatus;
import com.systemcontrol.backend.model.Priority;
import com.systemcontrol.backend.service.DefectService;
import com.systemcontrol.backend.service.ProjectService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    
    private final DefectService defectService;
    private final ProjectService projectService;
    private static final Logger log = LoggerFactory.getLogger(ReportController.class);
    
    public ReportController(DefectService defectService, ProjectService projectService) {
        this.defectService = defectService;
        this.projectService = projectService;
    }
    
    @GetMapping("/defects/export")
    public ResponseEntity<byte[]> exportDefectsToExcel(@RequestParam(required = false) Long projectId) {
        try {
            List<Defect> defects = projectId != null ? 
                defectService.listByProject(projectId) : 
                defectService.listAll();
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Workbook workbook = new XSSFWorkbook();
            log.info("Workbook created");
            Sheet sheet = workbook.createSheet("Defects Report");
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Title", "Description", "Priority", "Status", "Assignee ID", "Project ID", "Due Date", "Created At", "Updated At"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(createHeaderStyle(workbook));
            }
            
            // Create data rows
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            for (int i = 0; i < defects.size(); i++) {
                Defect defect = defects.get(i);
                Row row = sheet.createRow(i + 1);
                
                row.createCell(0).setCellValue(defect.getId());
                row.createCell(1).setCellValue(defect.getTitle());
                row.createCell(2).setCellValue(defect.getDescription() != null ? defect.getDescription() : "");
                row.createCell(3).setCellValue(defect.getPriority().toString());
                row.createCell(4).setCellValue(defect.getStatus().toString());
                row.createCell(5).setCellValue(defect.getAssigneeId() != null ? defect.getAssigneeId().toString() : "");
                row.createCell(6).setCellValue(defect.getProjectId() != null ? defect.getProjectId().toString() : "");
                row.createCell(7).setCellValue(defect.getDueDate() != null ? defect.getDueDate().toString() : "");
                row.createCell(8).setCellValue(defect.getCreatedAt() != null ? defect.getCreatedAt().toString() : "");
                row.createCell(9).setCellValue(defect.getUpdatedAt() != null ? defect.getUpdatedAt().toString() : "");
            }
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(outputStream);
            workbook.close();
            
            byte[] excelBytes = outputStream.toByteArray();
            
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            responseHeaders.setContentDispositionFormData("attachment", "defects_report.xlsx");
            
            return ResponseEntity.ok()
                    .headers(responseHeaders)
                    .body(excelBytes);
                    
        } catch (IOException e) {
            log.error("Failed to export defects report", e);
            return ResponseEntity.internalServerError().build();
        } catch (Exception e) {
            log.error("Unexpected error during defects export", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/full/export")
    public ResponseEntity<byte[]> exportFullToExcel() {
        try {
            log.info("Starting full report export...");
            var projects = projectService.listAll();
            log.info("Fetched {} projects", projects.size());
            var defects = defectService.listAll();
            log.info("Fetched {} defects", defects.size());

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Workbook workbook = new XSSFWorkbook();

            // Sheet 1: Projects
            Sheet projectsSheet = workbook.createSheet("Projects");
            Row pHeader = projectsSheet.createRow(0);
            String[] pHeaders = {"ID", "Name", "Description", "Start Date", "End Date"};
            for (int i = 0; i < pHeaders.length; i++) {
                Cell cell = pHeader.createCell(i);
                cell.setCellValue(pHeaders[i]);
                cell.setCellStyle(createHeaderStyle(workbook));
            }
            log.info("Projects header created");
            for (int i = 0; i < projects.size(); i++) {
                var prj = projects.get(i);
                Row row = projectsSheet.createRow(i + 1);
                row.createCell(0).setCellValue(prj.getId() != null ? prj.getId().toString() : "");
                row.createCell(1).setCellValue(prj.getName() != null ? prj.getName() : "");
                row.createCell(2).setCellValue(prj.getDescription() != null ? prj.getDescription() : "");
                row.createCell(3).setCellValue(prj.getStartDate() != null ? prj.getStartDate().toString() : "");
                row.createCell(4).setCellValue(prj.getEndDate() != null ? prj.getEndDate().toString() : "");
            }
            log.info("Projects rows filled: {}", projects.size());
            for (int i = 0; i < pHeaders.length; i++) projectsSheet.autoSizeColumn(i);
            log.info("Projects autosized");

            // Sheet 2: Defects
            Sheet defectsSheet = workbook.createSheet("Defects");
            Row dHeader = defectsSheet.createRow(0);
            String[] dHeaders = {"ID", "Title", "Description", "Priority", "Status", "Assignee ID", "Project ID", "Due Date", "Created At", "Updated At"};
            for (int i = 0; i < dHeaders.length; i++) {
                Cell cell = dHeader.createCell(i);
                cell.setCellValue(dHeaders[i]);
                cell.setCellStyle(createHeaderStyle(workbook));
            }
            log.info("Defects header created");
            for (int i = 0; i < defects.size(); i++) {
                Defect defect = defects.get(i);
                Row row = defectsSheet.createRow(i + 1);
                row.createCell(0).setCellValue(defect.getId() != null ? defect.getId().toString() : "");
                row.createCell(1).setCellValue(defect.getTitle() != null ? defect.getTitle() : "");
                row.createCell(2).setCellValue(defect.getDescription() != null ? defect.getDescription() : "");
                row.createCell(3).setCellValue(defect.getPriority() != null ? defect.getPriority().toString() : "");
                row.createCell(4).setCellValue(defect.getStatus() != null ? defect.getStatus().toString() : "");
                row.createCell(5).setCellValue(defect.getAssigneeId() != null ? defect.getAssigneeId().toString() : "");
                row.createCell(6).setCellValue(defect.getProjectId() != null ? defect.getProjectId().toString() : "");
                row.createCell(7).setCellValue(defect.getDueDate() != null ? defect.getDueDate().toString() : "");
                row.createCell(8).setCellValue(defect.getCreatedAt() != null ? defect.getCreatedAt().toString() : "");
                row.createCell(9).setCellValue(defect.getUpdatedAt() != null ? defect.getUpdatedAt().toString() : "");
            }
            log.info("Defects rows filled: {}", defects.size());
            for (int i = 0; i < dHeaders.length; i++) defectsSheet.autoSizeColumn(i);
            log.info("Defects autosized");

            log.info("Writing workbook to output stream...");
            workbook.write(outputStream);
            workbook.close();

            byte[] bytes = outputStream.toByteArray();
            log.info("Generated Excel file: {} bytes", bytes.length);
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            responseHeaders.setContentDispositionFormData("attachment", "full_report.xlsx");

            return ResponseEntity.ok().headers(responseHeaders).body(bytes);
        } catch (IOException e) {
            log.error("Failed to export full report (IOException)", e);
            return ResponseEntity.internalServerError().build();
        } catch (Exception e) {
            log.error("Unexpected error during full export (Exception)", e);
            return ResponseEntity.internalServerError().build();
        } catch (Throwable t) {
            log.error("Unexpected fatal error during full export (Throwable)", t);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> getAnalytics(@RequestParam(required = false) Long projectId) {
        List<Defect> defects = projectId != null ? 
            defectService.listByProject(projectId) : 
            defectService.listAll();
        
        Map<String, Object> analytics = Map.of(
            "totalDefects", defects.size(),
            "statusDistribution", defects.stream()
                .collect(Collectors.groupingBy(Defect::getStatus, Collectors.counting())),
            "priorityDistribution", defects.stream()
                .collect(Collectors.groupingBy(Defect::getPriority, Collectors.counting())),
            "newDefects", defects.stream()
                .filter(d -> d.getStatus() == DefectStatus.NEW)
                .count(),
            "inProgressDefects", defects.stream()
                .filter(d -> d.getStatus() == DefectStatus.IN_PROGRESS)
                .count(),
            "closedDefects", defects.stream()
                .filter(d -> d.getStatus() == DefectStatus.CLOSED)
                .count()
        );
        
        return ResponseEntity.ok(analytics);
    }
    
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }
}

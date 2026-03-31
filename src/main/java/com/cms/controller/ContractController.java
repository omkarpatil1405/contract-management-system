package com.cms.controller;

import com.cms.model.Contract;
import com.cms.model.User;
import com.cms.service.ContractService;
import com.cms.service.FileStorageService;
import com.cms.service.TextExtractionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/contracts")
public class ContractController {

    @Autowired
    private ContractService contractService;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private TextExtractionService textExtractionService;

    // ── Add Contract (GET) ────────────────────────────────────
    @GetMapping("/add")
    public String addContractPage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/login";

        model.addAttribute("contract", new Contract());
        model.addAttribute("currentUser", user);
        model.addAttribute("contractTypes", getContractTypes(user));
        return "add-contract";
    }

    // ── Add Contract (POST) ───────────────────────────────────
    @PostMapping("/add")
    public String addContract(Contract contract,
                              BindingResult bindingResult,
                              @RequestParam(value = "file", required = false) MultipartFile file,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/login";

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Invalid input. Please check your dates and try again.");
            return "redirect:/contracts/add";
        }

        contract.setUser(user);

        // Handle file upload
        if (file != null && !file.isEmpty()) {
            try {
                String fileName = fileStorageService.storeFile(file);
                contract.setFileName(fileName);
            } catch (IllegalArgumentException e) {
                redirectAttributes.addFlashAttribute("error", e.getMessage());
                return "redirect:/contracts/add";
            } catch (IOException e) {
                redirectAttributes.addFlashAttribute("error", "Failed to upload file. Please try again.");
                return "redirect:/contracts/add";
            }
        }

        contractService.saveContract(contract);
        redirectAttributes.addFlashAttribute("success", "Contract added successfully");
        return "redirect:/dashboard";
    }

    // ── View Contract (GET) ──────────────────────────────────
    @GetMapping("/view/{id}")
    public String viewContract(@PathVariable Long id,
                               HttpSession session,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/login";

        Optional<Contract> optionalContract = contractService.findById(id);
        if (optionalContract.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Contract not found");
            return "redirect:/dashboard";
        }

        Contract contract = optionalContract.get();
        if (user.getRole() != User.Role.ADMIN && !contract.getUser().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("error", "Access denied");
            return "redirect:/dashboard";
        }

        model.addAttribute("contract", contract);
        model.addAttribute("currentUser", user);

        // Extract text from attachment if present
        String fileName = contract.getFileName();
        if (fileName != null && !fileName.isEmpty()) {
            model.addAttribute("isImage", textExtractionService.isImage(fileName));
            model.addAttribute("isPdf", textExtractionService.isPdf(fileName));

            if (textExtractionService.isPdf(fileName)) {
                try {
                    Path filePath = fileStorageService.loadFile(fileName);
                    String text = textExtractionService.extractText(filePath);
                    model.addAttribute("extractedText", text);
                } catch (Exception e) {
                    model.addAttribute("extractedText", "[Could not extract text: " + e.getMessage() + "]");
                }
            }
        }

        return "view-contract";
    }

    // ── Edit Contract (GET) ───────────────────────────────────
    @GetMapping("/edit/{id}")
    public String editContractPage(@PathVariable Long id,
                                   HttpSession session,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/login";

        Optional<Contract> optionalContract = contractService.findById(id);
        if (optionalContract.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Contract not found");
            return "redirect:/dashboard";
        }

        Contract contract = optionalContract.get();
        if (user.getRole() != User.Role.ADMIN && !contract.getUser().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("error", "Access denied");
            return "redirect:/dashboard";
        }

        model.addAttribute("contract", contract);
        model.addAttribute("currentUser", user);
        model.addAttribute("contractTypes", getContractTypes(user));
        return "edit-contract";
    }

    // ── Edit Contract (POST) ──────────────────────────────────
    @PostMapping("/edit/{id}")
    public String editContract(@PathVariable Long id,
                               Contract updatedContract,
                               BindingResult bindingResult,
                               @RequestParam(value = "file", required = false) MultipartFile file,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/login";

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", "Invalid input. Please check your dates and try again.");
            return "redirect:/contracts/edit/" + id;
        }

        Optional<Contract> optionalContract = contractService.findById(id);
        if (optionalContract.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Contract not found");
            return "redirect:/dashboard";
        }

        Contract contract = optionalContract.get();
        if (user.getRole() != User.Role.ADMIN && !contract.getUser().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("error", "Access denied");
            return "redirect:/dashboard";
        }

        contract.setTitle(updatedContract.getTitle());
        contract.setDescription(updatedContract.getDescription());
        contract.setStartDate(updatedContract.getStartDate());
        contract.setEndDate(updatedContract.getEndDate());
        contract.setStatus(updatedContract.getStatus());
        contract.setParty(updatedContract.getParty());
        contract.setContractType(updatedContract.getContractType());

        // Handle file upload (replace old file if new one uploaded)
        if (file != null && !file.isEmpty()) {
            try {
                // Delete old file if exists
                if (contract.getFileName() != null) {
                    fileStorageService.deleteFile(contract.getFileName());
                }
                String fileName = fileStorageService.storeFile(file);
                contract.setFileName(fileName);
            } catch (IllegalArgumentException e) {
                redirectAttributes.addFlashAttribute("error", e.getMessage());
                return "redirect:/contracts/edit/" + id;
            } catch (IOException e) {
                redirectAttributes.addFlashAttribute("error", "Failed to upload file. Please try again.");
                return "redirect:/contracts/edit/" + id;
            }
        }

        contractService.saveContract(contract);
        redirectAttributes.addFlashAttribute("success", "Contract updated successfully");
        return "redirect:/dashboard";
    }

    // ── Delete Contract ───────────────────────────────────────
    @PostMapping("/delete/{id}")
    public String deleteContract(@PathVariable Long id,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/login";

        Optional<Contract> optionalContract = contractService.findById(id);
        if (optionalContract.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Contract not found");
            return "redirect:/dashboard";
        }

        Contract contract = optionalContract.get();
        if (user.getRole() != User.Role.ADMIN && !contract.getUser().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("error", "Access denied");
            return "redirect:/dashboard";
        }

        // Delete associated file
        if (contract.getFileName() != null) {
            fileStorageService.deleteFile(contract.getFileName());
        }

        contractService.deleteContract(id);
        redirectAttributes.addFlashAttribute("success", "Contract deleted successfully");
        return "redirect:/dashboard";
    }

    // ── Download File ─────────────────────────────────────────
    @GetMapping("/download/{fileName}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName,
                                                  HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            Path filePath = fileStorageService.loadFile(fileName);
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            // Determine content type
            String contentType = "application/octet-stream";
            String name = fileName.toLowerCase();
            if (name.endsWith(".pdf")) {
                contentType = "application/pdf";
            } else if (name.endsWith(".jpg") || name.endsWith(".jpeg")) {
                contentType = "image/jpeg";
            } else if (name.endsWith(".png")) {
                contentType = "image/png";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);

        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ── Helper: Build dynamic contract type list ───────────────
    private Set<String> getContractTypes(User user) {
        Set<String> types = new LinkedHashSet<>(Arrays.asList(Contract.DEFAULT_CONTRACT_TYPES));
        List<Contract> allContracts = contractService.getContractsForUser(user);
        for (Contract c : allContracts) {
            if (c.getContractType() != null && !c.getContractType().isEmpty()) {
                types.add(c.getContractType());
            }
        }
        return types;
    }
}

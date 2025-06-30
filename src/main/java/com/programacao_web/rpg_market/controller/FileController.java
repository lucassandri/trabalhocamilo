package com.programacao_web.rpg_market.controller;

import com.programacao_web.rpg_market.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/images")
public class FileController {

    private static final Logger log = LoggerFactory.getLogger(FileController.class);
    
    // Contador para monitorar uso excessivo da imagem padrão
    private static int defaultImageCounter = 0;
    private static long lastLogTime = 0;

    @Autowired
    private FileStorageService fileStorageService;    @GetMapping("/{fileName:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String fileName) {
        try {
            // Caso especial: imagem padrão de produto
            if ("default-product.jpg".equals(fileName) || "default-user.jpg".equals(fileName)) {
                return serveStaticImage(fileName);
            }
            
            // Tentar carregar de arquivos estáticos primeiro
            try {
                Resource staticResource = new UrlResource(getClass().getClassLoader().getResource("static/images/" + fileName));
                if (staticResource.exists()) {
                    return createImageResponse(staticResource, fileName, 1800);
                }
            } catch (IOException ioEx) {
                log.debug("Arquivo estático não encontrado: {}", fileName);
            } catch (SecurityException secEx) {
                log.warn("Erro de segurança ao acessar arquivo estático {}: {}", fileName, secEx.getMessage());
            } catch (RuntimeException runtimeEx) {
                log.warn("Erro de runtime ao acessar arquivo estático {}: {}", fileName, runtimeEx.getMessage());
            }
            
            // Tentar carregar de uploads
            try {
                Path filePath = fileStorageService.getFilePath(fileName);
                Resource resource = new UrlResource(filePath.toUri());
                
                if (resource.exists()) {
                    return createImageResponse(resource, fileName, 1800);
                }
            } catch (IOException ioEx) {
                // Tratar IOException específicas de conexão cancelada
                String message = ioEx.getMessage();
                if (message != null && (message.contains("connection was cancelled") || 
                                      message.contains("conexão anulada") ||
                                      message.contains("Connection reset") ||
                                      message.contains("Broken pipe"))) {
                    log.debug("Cliente desconectou durante o carregamento de: {}", fileName);
                } else {
                    log.debug("Arquivo de upload não encontrado: {}", fileName);
                }
            } catch (SecurityException secEx) {
                log.warn("Erro de segurança ao acessar upload {}: {}", fileName, secEx.getMessage());
                return ResponseEntity.status(403).build();
            } catch (RuntimeException runtimeEx) {
                log.warn("Erro de runtime ao acessar upload {}: {}", fileName, runtimeEx.getMessage());
            }
            
            // Fallback para imagem padrão em arquivos UUID não encontrados
            if (fileName.matches("^[a-f0-9-]{36}\\.(jpg|jpeg|png|gif|webp)$") || 
                fileName.startsWith("profile-") || 
                (!fileName.equals("default-product.jpg") && !fileName.equals("default-user.jpg"))) {
                
                // Monitorar uso excessivo da imagem padrão
                defaultImageCounter++;
                long currentTime = System.currentTimeMillis();
                
                // Log apenas a cada 50 usos ou a cada 30 segundos para evitar spam
                if (defaultImageCounter % 50 == 0 || (currentTime - lastLogTime) > 30000) {
                    log.info("Imagem padrão usada {} vezes. Arquivo não encontrado: {}", defaultImageCounter, fileName);
                    lastLogTime = currentTime;
                }
                
                return serveDefaultImage();
            }
            
            log.debug("Arquivo não encontrado: {}", fileName);
            return ResponseEntity.notFound().build();
            
        } catch (SecurityException secEx) {
            log.warn("Erro de segurança ao servir arquivo {}: {}", fileName, secEx.getMessage());
            return ResponseEntity.status(403).build();
        } catch (RuntimeException runtimeEx) {
            log.error("Erro de runtime ao servir arquivo {}: {}", fileName, runtimeEx.getMessage());
            return ResponseEntity.status(500).build();
        } catch (Exception e) {
            log.error("Erro inesperado ao servir arquivo {}: {}", fileName, e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    private ResponseEntity<Resource> serveStaticImage(String fileName) {
        try {
            Resource resource = new UrlResource(getClass().getClassLoader().getResource("static/images/" + fileName));
            if (resource.exists()) {
                return createImageResponse(resource, fileName, 3600); // Cache por 1 hora
            }
        } catch (IOException ioEx) {
            log.error("Erro de I/O ao carregar imagem padrão {}: {}", fileName, ioEx.getMessage());
        } catch (SecurityException secEx) {
            log.error("Erro de segurança ao carregar imagem padrão {}: {}", fileName, secEx.getMessage());
        } catch (RuntimeException runtimeEx) {
            log.error("Erro de runtime ao carregar imagem padrão {}: {}", fileName, runtimeEx.getMessage());
        }
        return ResponseEntity.notFound().build();
    }

    private ResponseEntity<Resource> serveDefaultImage() {
        try {
            Resource defaultResource = new UrlResource(getClass().getClassLoader().getResource("static/images/default-product.jpg"));
            if (defaultResource.exists()) {
                return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"default-product.jpg\"")
                    .header(HttpHeaders.CACHE_CONTROL, "public, max-age=3600") // Cache longo para imagem padrão
                    .header(HttpHeaders.ETAG, "\"default-product_v1\"")
                    .contentType(MediaType.IMAGE_JPEG)
                    .contentLength(defaultResource.contentLength())
                    .body(defaultResource);
            }
        } catch (IOException ioEx) {
            log.error("Erro de I/O ao carregar imagem padrão como fallback: {}", ioEx.getMessage());
        } catch (SecurityException secEx) {
            log.error("Erro de segurança ao carregar imagem padrão como fallback: {}", secEx.getMessage());
        } catch (RuntimeException runtimeEx) {
            log.error("Erro de runtime ao carregar imagem padrão como fallback: {}", runtimeEx.getMessage());
        }
        return ResponseEntity.notFound().build();
    }

    private ResponseEntity<Resource> createImageResponse(Resource resource, String fileName, long cacheMaxAge) throws IOException {
        MediaType mediaType = determineMediaType(fileName);
        String etag = "\"" + fileName + "_" + resource.lastModified() + "\"";
        
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
            .header(HttpHeaders.CACHE_CONTROL, "public, max-age=" + cacheMaxAge)
            .header(HttpHeaders.ETAG, etag)
            .contentType(mediaType)
            .contentLength(resource.contentLength())
            .body(resource);
    }
    
    @GetMapping("/debug/path")
    @ResponseBody
    public Map<String, Object> debugPath() {
        Map<String, Object> info = new HashMap<>();
        Path uploadDir = fileStorageService.getFilePath("");
        info.put("diretorioUpload", uploadDir.toString());
        info.put("existe", Files.exists(uploadDir));
        info.put("ehDiretorio", Files.isDirectory(uploadDir));
        info.put("ehLegivel", Files.isReadable(uploadDir));        
        try {
            List<String> files = Files.list(uploadDir)
                .map(path -> path.getFileName().toString())
                .sorted()
                .collect(Collectors.toList());
            info.put("arquivos", files);
        } catch (Exception e) {
            info.put("erroArquivos", e.getMessage());
        }
        
        return info;
    }

    @GetMapping("/debug/test")
    @ResponseBody
    public String test() {
        return "FileController funcionando!";
    }

    @GetMapping("/debug/stats")
    @ResponseBody
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("defaultImageUsageCount", defaultImageCounter);
        stats.put("lastLogTime", lastLogTime);
        stats.put("applicationStatus", "running");
        return stats;
    }

    private MediaType determineMediaType(String fileName) {
        if (fileName.toLowerCase().endsWith(".jpg") || fileName.toLowerCase().endsWith(".jpeg")) {
            return MediaType.IMAGE_JPEG;
        } else if (fileName.toLowerCase().endsWith(".png")) {
            return MediaType.IMAGE_PNG;
        } else if (fileName.toLowerCase().endsWith(".gif")) {
            return MediaType.IMAGE_GIF;
        } else {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}
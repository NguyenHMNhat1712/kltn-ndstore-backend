package com.example.officepcstore.payload.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryReq {
    private String name;
    private String parent_category = "-1";
    private MultipartFile file;
    private String state;
}

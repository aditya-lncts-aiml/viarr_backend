// Resume.java
package com.viarr.viarr_backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;

    @Lob
    @Column(name = "file_data", columnDefinition = "LONGBLOB")
    private byte[] fileData;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    // ✅ Add this constructor for convenience
    public Resume(String fileName, byte[] fileData, User user) {
        this.fileName = fileName;
        this.fileData = fileData;
        this.user = user;
    }
}

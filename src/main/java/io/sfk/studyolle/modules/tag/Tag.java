package io.sfk.studyolle.modules.tag;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@Getter @Setter
@EqualsAndHashCode(of = "id")
@AllArgsConstructor
@NoArgsConstructor
public class Tag {

    @Id @GeneratedValue
    private Long id;

    @Column(unique = true, nullable = false)
    private String title;

}

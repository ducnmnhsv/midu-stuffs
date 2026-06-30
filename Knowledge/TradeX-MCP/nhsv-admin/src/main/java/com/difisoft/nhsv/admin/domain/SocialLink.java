package com.difisoft.nhsv.admin.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import javax.persistence.*;

/**
 * A SocialLink.
 */
@Entity
@Table(name = "t_social_link")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class SocialLink implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "type")
    private String type;

    @Column(name = "link")
    private String link;

    @ManyToOne
    @JsonIgnore
    @JsonIgnoreProperties(value = { "socialLinks" }, allowSetters = true)
    private ChatRoom chatRoom;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public SocialLink id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return this.type;
    }

    public SocialLink type(String type) {
        this.setType(type);
        return this;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLink() {
        return this.link;
    }

    public SocialLink link(String link) {
        this.setLink(link);
        return this;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public ChatRoom getChatRoom() {
        return this.chatRoom;
    }

    public void setChatRoom(ChatRoom chatRoom) {
        this.chatRoom = chatRoom;
    }

    public SocialLink chatRoom(ChatRoom chatRoom) {
        this.setChatRoom(chatRoom);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SocialLink)) {
            return false;
        }
        return id != null && id.equals(((SocialLink) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "SocialLink{" +
            "id=" + getId() +
            ", type='" + getType() + "'" +
            ", link='" + getLink() + "'" +
            "}";
    }
}

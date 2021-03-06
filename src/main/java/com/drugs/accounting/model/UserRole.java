package com.drugs.accounting.model;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Entity
@Table(name = "user_role"/*, schema = "sign"*/)
@IdClass(UserRoleId.class)
public class UserRole {
	
	@Id
	@Column(name = "user_id")
	Long userId;
	
	@Id
	@Column(name = "role_id")
	Long roleId;
	
	@Id
	@Column(name = "start_date")
	Timestamp dateStart;
	
	@Id
	@Column(name = "end_date")
	Timestamp dateEnd;
}

package com.drugs.accounting.model;

import java.io.Serializable;
import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = {"userId", "roleId", "dateStart", "dateEnd"})
public class UserRoleId implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3895633133754208600L;
	
	Long userId;
	Long roleId;
	Timestamp dateStart;
	Timestamp dateEnd;
	
}

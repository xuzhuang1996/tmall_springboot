

package com.tmall.pojo;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Set;

@Entity
@Table(name = "user")
@JsonIgnoreProperties({ "handler","hibernateLazyInitializer" })
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) 
	@Column(name = "id")		
	private int id;
	
	private String password;
	private String name;	
	private String salt;

	//做了一个优惠券,默认生成的关联表名称为主表表名+下划线+从表表名,详情看https://blog.csdn.net/johnf_nash/article/details/80642581
	//不过照着这个写会出现懒加载错误，解决https://blog.csdn.net/cainiao_accp/article/details/68922320
//	@ManyToMany(cascade=CascadeType.REFRESH, fetch = FetchType.EAGER)cascade = CascadeType.ALL
//	@JoinTable(
//			name="user_coupon",
//			joinColumns=@JoinColumn(name="user_id"),
//			inverseJoinColumns=@JoinColumn(name="coupon_id")
//	)
	@ManyToMany(cascade=CascadeType.REFRESH, fetch = FetchType.EAGER)
	@JoinTable(name = "user_coupon",
			joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
			inverseJoinColumns = @JoinColumn(name = "coupon_id", referencedColumnName = "id"))
	private List<Coupon> coupons;//一个用户有多个优惠券
	
	@Transient
	private String anonymousName;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSalt() {
		return salt;
		
	}

	public void setSalt(String salt) {
		this.salt = salt;
	}

	public String getAnonymousName(){
		if(null!=anonymousName)
			return anonymousName;
		if(null==name)
			anonymousName= null;
		else if(name.length()<=1)
			anonymousName = "*";
		else if(name.length()==2)
			anonymousName = name.substring(0,1) +"*";
		else {
			char[] cs =name.toCharArray();
			for (int i = 1; i < cs.length-1; i++) {
				cs[i]='*';
			}
			anonymousName = new String(cs);			
		}
		return anonymousName;
	}

	public void setAnonymousName(String anonymousName) {
		this.anonymousName = anonymousName;
	}

	public List<Coupon> getCoupons() {
		return coupons;
	}

	public void setCoupons(List<Coupon> coupons) {
		this.coupons = coupons;
	}
}

/**
 * 功能说明:
 * 功能作者:
 * 创建日期:
 * 版权归属:每特教育|蚂蚁课堂所有 www.itmayiedu.com
 */
package com.itmayiedu.api.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.itmayiedu.api.entity.UserEntity;
import com.itmayiedu.api.fallback.MemberFallback;
import com.itmayiedu.api.feign.MemberServiceFeigin;
import com.itmayiedu.api.service.IOrderService;
import com.itmayiedu.base.BaseApiService;
import com.itmayiedu.base.ResponseBase;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

/**
 * 功能说明: <br>
 * 创建作者:每特教育-余胜军<br>
 * 创建时间:2018年9月6日 下午10:31:04<br>
 * 教育机构:每特教育|蚂蚁课堂<br>
 * 版权说明:上海每特教育科技有限公司版权所有<br>
 * 官方网站:www.itmayiedu.com|www.meitedu.com<br>
 * 联系方式:qq644064779<br>
 * 注意:本内容有每特教育学员共同研发,请尊重原创版权
 */
@RestController
public class OrderServiceImpl extends BaseApiService implements IOrderService {
	// 订单服务继承会员服务接口，用来实现feign客户端 减少重复接口代码
	@Autowired
	private MemberServiceFeigin memberServiceFeigin;

	@RequestMapping("/orderToMember")
	public String orderToMember(String name) {
		UserEntity user = memberServiceFeigin.getMember(name);
		return user == null ? "没有找到用户信息" : user.toString();
	}

	// 没有解决服务雪崩效应
	@RequestMapping("/orderToMemberUserInfo")
	public ResponseBase orderToMemberUserInfo() {
		/**
		 * @FeignClient(value = "app-itmayiedu-member", fallback = MemberFallback.class)
		 * 
		 *  接口视线中存在默认的 fallback 方法 如果生产者出现异常 则 调用默认的fallback 方法 MemberFallback 同方法名的函数
		 *  开启Hystrix  断路器  则认为不设置超时等待，如果出现等待直接调用 fallback
		 */
		return memberServiceFeigin.getUserInfo();
	}

	// 解决服务雪崩效应
	// fallbackMethod 方法的作用：服务降级执行
	// @HystrixCommand 默认开启线程池隔离方式,服务降级,服务熔断
	// 设置Hystrix服务超时时间
	/**
	 * @HystrixCommand<br>
	 * 					默认开启服务隔离方式 以线程池方式<br>
	 *                     默认开启服务降级执行方法orderToMemberUserInfoHystrixFallback<br>
	 *                     默认开启服务熔断机制<br>
	 * 
	 * @return
	 */
	@HystrixCommand(fallbackMethod = "orderToMemberUserInfoHystrixFallback")
	@RequestMapping("/orderToMemberUserInfoHystrix")
	public ResponseBase orderToMemberUserInfoHystrix() {
		System.out.println("orderToMemberUserInfoHystrix:" + "线程池名称:" + Thread.currentThread().getName());
		return memberServiceFeigin.getUserInfo();
	}

	// @HystrixCommand(fallbackMethod = "orderToMemberUserInfoHystrixFallback")
	@RequestMapping("/orderToMemberUserInfoHystrix02")
	public ResponseBase orderToMemberUserInfoHystrix02() {
		System.out.println("orderToMemberUserInfoHystrix:" + "线程池名称:" + Thread.currentThread().getName());
		return memberServiceFeigin.getUserInfo();
	}

	public ResponseBase orderToMemberUserInfoHystrixFallback() {
		return setResultSuccess("返回一个友好的提示：服务降级,服务器忙，请稍后重试!");
	}

	// 订单服务接口
	@RequestMapping("/orderInfo")
	public ResponseBase orderInfo() {
		System.out.println("orderInfo:" + "线程池名称:" + Thread.currentThread().getName());
		return setResultSuccess();
	}

	// Hystrix 有两种方式配置保护服务 通过注解和接口形式

}

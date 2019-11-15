
//登录后返回页面的URL
var referrer = "";

// alert(!referrer);//true
// alert(referrer);//""
referrer = document.referrer;
// alert(referrer);//地址
// alert(!referrer);//false

if (!referrer) {
	try {
		if (window.opener) {                
			// IE下如果跨域则抛出权限异常，Safari和Chrome下window.opener.location没有任何属性              
			referrer = window.opener.location.href;
		}  
	} catch (e) {
	}
}

//按键盘Enter键即可登录
$(document).keyup(function(event){
	if(event.keyCode == 13){
		login();
	}
});


/*$(document).ready(function () {

});*/

$(function () {
	$("#loginBtn").on("click",function () {
		var flag = true;

		var phone = $.trim($("#phone").val());
		var loginPassword = $.trim($("#loginPassword").val());
		// var captcha = $.trim($("#captcha").val());
		var messageCode = $.trim($("#messageCode").val());

		if ("" == phone) {
			$("#showId").html("请输入手机号码");
			flag = false;
			return;
		} else if (!/^1[1-9]\d{9}$/.test(phone)) {
			$("#showId").html("请输入正确的手机号码");
			flag = false;
			return;
		} else if ("" == loginPassword) {
			$("#showId").html("请输入登录密码");
			flag = false;
			return;
		} else if (loginPassword.length < 6 || loginPassword.length > 20) {
			flag = false;
			$("#showId").html("登录密码长度应为6-20位");
			return ;
		} else if("" == messageCode){
			flag = false;
			$("#showId").html("请输入短信验证码");
			return;
		} else {
			/*$.ajax({
				url:"loan/checkCaptcha",
				type:"post",
				data:{
					"captcha":captcha
				},
				async:false,//关闭异步请求
				success:function (data) {
					if (data.code == "10000") {
						$("#showId").html("");
					} else {
						$("#showId").html(data.message);
						flag = false;
					}
				},
				error:function () {
					$("#showId").html("系统繁忙，请稍后重试");
					flag = false;
				}
			});*/
			$("#showId").html("");
		}


		if (flag) {

			$("#loginPassword").val($.md5(loginPassword));

			$.ajax({
				url:"loan/login",
				type:"post",
				data:{
					"phone":phone,
					"loginPassword":$.md5(loginPassword),
					"messageCode":messageCode
				},
				success:function (data) {
					if (data.code == "10000") {
						window.location.href = referrer;
					} else {
						$("#loginPassword").val("");
						$("#showId").html(data.message);
					}
				},
				error:function () {
					$("#loginPassword").val("");
					$("#showId").html("登录异常，请重试");
				}
			});

		}


	});
	
	
	$("#dateBtn1").on("click",function () {
		var flag = true;

		if (!$("#dateBtn1").hasClass("on")) {

			var phone = $.trim($("#phone").val());
			var loginPassword = $.trim($("#loginPassword").val());

			if ("" == phone) {
				$("#showId").html("请输入手机号码");
				flag = false;
				return;
			} else if (!/^1[1-9]\d{9}$/.test(phone)) {
				$("#showId").html("请输入正确的手机号码");
				flag = false;
				return;
			} else if ("" == loginPassword) {
				$("#showId").html("请输入登录密码");
				flag = false;
				return;
			} else if (loginPassword.length < 6 || loginPassword.length > 20) {
				flag = false;
				$("#showId").html("登录密码长度应为6-20位");
				return;
			} else {
				$("#showId").html("");
			}

			if (flag) {

				$.ajax({
					url:"loan/messageCode",
					type:"post",
					data:"phone="+phone,
					success:function (data) {
						if (data.code == "10000") {
							alert("您的短信验证码是：" + data.data);
							$.leftTime(60,function (d) {
								if (d.status) {
									$("#dateBtn1").addClass("on");
									$("#dateBtn1").html((d.s == "00"?"60":d.s) + "s后获取");
								} else {
									$("#dateBtn1").removeClass("on");
									$("#dateBtn1").html("获取验证码");
								}
							});
						} else {
							$("#showId").html(data.message);
						}
					},
					error:function () {
						$("#showId").html("系统繁忙，请稍后重试");
					}
				});




			}





		}

	});
	
	
});
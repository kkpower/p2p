
//同意实名认证协议
$(function() {
	$("#agree").click(function(){
		var ischeck = document.getElementById("agree").checked;
		if (ischeck) {
			$("#btnRegist").attr("disabled", false);
			$("#btnRegist").removeClass("fail");
		} else {
			$("#btnRegist").attr("disabled","disabled");
			$("#btnRegist").addClass("fail");
		}
	});

	//真实姓名
	$("#realName").on("blur",function () {
		var realName = $.trim($("#realName").val());

		if ("" == realName) {
			showError("realName","请输入真实姓名");
		} else if (!/^[\u4e00-\u9fa5]{0,}$/.test(realName)) {
			showError("realName","真实姓名只支持中文");
		} else {
			showSuccess("realName");
		}
	});

	$("#idCard").on("blur",function () {
		var idCard = $.trim($("#idCard").val());

		if ("" == idCard) {
			showError("idCard","请输入身份证号码");
		} else if (!/(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/.test(idCard)) {
			showError("idCard", "请输入正确的身份证号码");
		} else {
			showSuccess("idCard");
		}
	});

	$("#captcha").on("blur",function () {
		var captcha = $.trim($("#captcha").val());

		if ("" == captcha) {
			showError("captcha", "请输入图形验证码");
		} else {
			$.ajax({
				url:"loan/checkCaptcha",
				type:"post",
				data:{
					"captcha":captcha
				},
				success:function (data) {
					if (data.code == "10000") {
						showSuccess("captcha");
					} else {
						showError("captcha",data.message);
					}
				},
				error:function () {
					showError("captcha","系统繁忙，请稍后重试");
				}
			});
		}
	});

	$("#btnRegist").on("click",function () {
		var errorText = "";
		var idCard = $.trim($("#idCard").val());
		var realName = $.trim($("#realName").val());

		$("#realName").blur();
		errorText = $("#realNameErr").text();
		if ("" != errorText) {
			return false;
		}

		$("#idCard").blur();
		errorText = $("#idCardErr").text();
		if ("" != errorText) {
			return false;
		}

		$("#captcha").blur();
		errorText = $("#captchaErr").text();
		if ("" != errorText) {
			return false;
		}

		$.ajax({
			url:"loan/verifyRealName",
			type:"post",
			data:{
				"idCard":idCard,
				"realName":realName
			},
			success:function (data) {
				if (data.code == "10000") {
					window.location.href = "index";
				} else {
					showError("captcha",data.message);
				}
			},
			error:function () {
				showError("captcha","系统繁忙，请稍后重试")
			}

		});

	});

});
//打开注册协议弹层
function alertBox(maskid,bosid){
	$("#"+maskid).show();
	$("#"+bosid).show();
}
//关闭注册协议弹层
function closeBox(maskid,bosid){
	$("#"+maskid).hide();
	$("#"+bosid).hide();
}

//错误提示
function showError(id,msg) {
	$("#"+id+"Ok").hide();
	$("#"+id+"Err").html("<i></i><p>"+msg+"</p>");
	$("#"+id+"Err").show();
	$("#"+id).addClass("input-red");
}
//错误隐藏
function hideError(id) {
	$("#"+id+"Err").hide();
	$("#"+id+"Err").html("");
	$("#"+id).removeClass("input-red");
}
//显示成功
function showSuccess(id) {
	$("#"+id+"Err").hide();
	$("#"+id+"Err").html("");
	$("#"+id+"Ok").show();
	$("#"+id).removeClass("input-red");
}
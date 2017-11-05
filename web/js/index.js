//服务器地址
var SA = 'https://api.spicybar.cn/';
//用户状态
var userStatus = 'offline';
//页面显示的昵称
var showUsername = '游客';
//用户id
var showUserId = 'null';

//窗口大小改变事件
function response() {
    var chatBox = $('#chatBox');
    var chatBody = $('#chatBody');
    if ($(window).height() > 667 && $(window).width() > 767) {
        chatBox.css('height', '498px');
        chatBody.css('margin-top', (($(window).height() - chatBody.height()) / 2) + 'px');
    } else {
        chatBox.css('height', ($(window).height() - 170) + 'px');
        chatBody.css('margin-top', '');
    }
    if (navigator.userAgent.indexOf('ChatRobotApp') !== -1) {
        $('#voiceBtn').removeClass('hidden');
    } else {
        $('#voiceBtn').addClass('hidden');
    }
}

//获取URL中的参数
function getQueryString(param) {
    var regular = new RegExp('(^|&)' + param + '=([^&]*)(&|$)');
    var encryptValue = window.location.hash.substr(1).match(regular);
    var value = null;
    if (encryptValue !== null) {
        value = decodeURI(encryptValue[2]);
    }
    return value;
}

//返回HH:mm:ss格式化后的时间
function getNowTime() {
    var nowDate = new Date();
    var nowTime = nowDate.toLocaleTimeString();
    var nowHours = nowDate.getHours();
    if (nowHours < 10) {
        nowHours = '0' + nowHours;
    }
    nowTime = nowTime.substring(nowTime.indexOf(':'), nowTime.length);
    return nowHours + nowTime;
}

//将时间戳格式化为可读日期格式
function getReadTime(timestamp) {
    var date = new Date(timestamp);
    var Y = date.getFullYear() + '-';
    var M = (date.getMonth() + 1 < 10 ? '0' + (date.getMonth() + 1) : date.getMonth() + 1) + '-';
    var D = (date.getDate() < 10 ? '0' + (date.getDate()) : date.getDate()) + ' ';
    var h = (date.getHours() < 10 ? '0' + date.getHours() : date.getHours()) + ':';
    var m = (date.getMinutes() < 10 ? '0' + date.getMinutes() : date.getMinutes()) + ':';
    var s = (date.getSeconds() < 10 ? '0' + date.getSeconds() : date.getSeconds());
    return Y + M + D + h + m + s;
}

//将时间戳格式化为日月
function getReadMonth(timestamp) {
    var date = new Date(timestamp);
    var M = (date.getMonth() + 1 < 10 ? '0' + (date.getMonth() + 1) : date.getMonth() + 1) + '月';
    var D = (date.getDate() < 10 ? '0' + (date.getDate()) : date.getDate()) + '日';
    return M + D;
}

//扩展方法，清除两边空格
String.prototype.trim = function () {
    return this.replace(/^\s+|\s+$/g, '');
};

//自动补全邮箱
var autoCompleteOption = {
    source: [{
        list: [
            {value: '@qq.com', text: '@qq.com'},
            {value: '@163.com', text: '@163.com'},
            {value: '@sina.com', text: '@sina.com'},
            {value: '@sohu.com', text: '@sohu.com'},
            {value: '@gmail.com', text: '@gmail.com'},
            {value: '@hotmail.com', text: '@hotmail.com'}
        ]
    }],
    getText: 'text',
    getValue: 'value',
    anchor: '@',
    limit: 6,
    debounce: 0,
    highlighter: false,
    highlightCompleteWords: false
};
horsey(document.querySelector('#loginUserEmail'), autoCompleteOption);
horsey(document.querySelector('#regUserEmail'), autoCompleteOption);
$('.horseyInput').focus(function () {
    $('.sey-container').removeClass('hidden');
}).blur(function () {
    $('.sey-container').addClass('hidden');
});

//用户选择地区事件
function selectArea(component) {
    component = $(component);
    $('#' + component.attr('aria-controls')).collapse('toggle');
}

//用户选择省事件
function chooseProvince(component) {
    var province = component.innerHTML;
    if (province.length === 2) {
        province = province + '&nbsp;&nbsp;&nbsp;';
    }
    $('#provinceBtn').html(province + "&nbsp;<span class='caret'></span>");
}

//热门搜索框横向滚轮事件
$('#hotSearchBox').mousewheel(function (event) {
    this.scrollLeft -= (event.deltaY * 10);
    event.preventDefault();
});

//进入店铺
function sellersEnterShop(sellerId) {
    if (sellerId !== 'null') {
        location.hash = 'sellerId=' + sellerId;
    } else {
        location.hash = 'sellerId=' + showUserId;
    }
    initPage();
    $('#adminModal').modal('hide');
}

//返回首页
function returnIndex() {
    location.hash = '';
    initPage();
}

//配置Ajax的默认设置
$.ajaxSetup({
    type: 'POST',
    crossDomain: true,
    xhrFields: {
        withCredentials: true
    }
});

//初始化页面信息
function initPage() {
    userStatus = 'offline';
    showUsername = '游客';
    showUserId = 'null';
    $('#openAdminBtn').addClass('hidden');
    $('#indexBtn').addClass('hidden');
    $('#sellerName').addClass('hidden');
    $('title').html('小明智能导购机器人');
    $.ajax({
        url: SA + 'userController/initPage.do',
        data: {
            sellerId: getQueryString('sellerId')
        },
        dataType: 'json',
        success: function (data) {
            if (data['accessSeller'] === 'true') {
                $('#sellerName').html('（店铺：' + data['sellerName'] + '）').removeClass('hidden');
                $('title').html(data['sellerName'] + '的店铺-小明智能导购机器人');
                acceptMessage('robot', '欢迎来到' + data['sellerName'] + '的店铺。');
                $('#indexBtn').removeClass('hidden');
            }
            userStatus = data['userStatus'];
            if (userStatus === 'online') {
                showUsername = data['username'];
                showUserId = data['userId'];
                if (data['userRole'] === '商户') {
                    $('#openAdminBtn').removeClass('hidden');
                }
                acceptMessage('robot', showUsername + '，别来无恙呀。');
            } else if (userStatus === 'offline') {
                acceptMessage('robot', '你好呀，我是智能导购机器人小明。登录可享更多功能哦，快试试对我说 "登录" 或 "注册" 吧');
            } else {
                acceptMessage('robot', '发生错误！检查下你的网络呗。');
            }
            hotSearch();
        },
        error: function () {
            acceptMessage('robot', '发生错误！检查下你的网络呗。');
        }
    });
    $('#message').focus();
}

//在网页上的显示发送或者接受到的消息
function acceptMessage(sender, message) {
    var chatDiv;
    if (sender === 'robot') {
        chatDiv = "<table class='text-left' width='100%' style='margin-bottom:20px;'><tr><td rowspan='2' width='50px'><img src='img/robot.svg' width='50px' height='50px' alt='机器人头像'/></td><td class='bubbleName'>小明&nbsp;&nbsp;" + getNowTime() + "</td></tr><tr><td><div style='width: 100%;'><span class='bubble leftBubble'>" + message + "<span class='bottomLevel'></span><span class='topLevel'></span></span></div></td></tr></table>";
    }
    if (sender === 'user') {
        chatDiv = "<table class='text-right' width='100%' style='margin-bottom:20px;'><tr><td class='bubbleName'>" + showUsername + "&nbsp;&nbsp;" + getNowTime() + "</td><td rowspan='2' width='50px'><img src='img/user.svg' width='50px' height='50px' alt='用户头像'/></td></tr><tr><td><div style='width: 100%;'><span class='bubble rightBubble text-left'>" + message + "<span class='bottomLevel'></span><span class='topLevel'></span></span></div></td></tr></table>";
    }
    var chatBox = $('#chatBox');
    chatBox.append(chatDiv).scrollTop(chatBox[0].scrollHeight);
}

//监听键盘回车事件，用以发送消息
function sendMessageEnter(event) {
    if (event.which === 13) {
        sendMessage();
    }
}

//热搜
function hotSearch() {
    var hotSearch = $('#hotSearch');
    hotSearch.html("<div class='text-center'><img src='img/loading.svg' class='loading' style='width: 31px;height: 31px;' alt='加载图片'/><span style='font-size: 1.6em;'>&nbsp;请稍后</span></div>");
    var sellerId = getQueryString('sellerId');
    $.ajax({
        url: SA + 'featuresController/hotSearch.do',
        data: {
            sellerId: sellerId
        },
        dataType: 'json',
        success: function (data) {
            if (!jQuery.isEmptyObject(data)) {
                var randomSort = Math.floor(Math.random() * data['hotGoods'].length);
                $('#message').attr('placeholder', '试试：帮我找找' + data['hotGoods'][randomSort]['sort']);
                hotSearch.html('');
                if (!jQuery.isEmptyObject(sellerId)) {
                    hotSearch.html("<span style='margin: 5px;'>店铺功能:</span>");
                    for (var i2 = 0; i2 < data['headOff'].length; i2++) {
                        if (i2 === 5) {
                            break;
                        }
                        hotSearch.append("<button style='border-radius: 20px;margin: 5px;' class='btn btn-default' type='button' onclick='onclickHotSearch(this)'>" + data['headOff'][i2]['pattern'] + "</button>");
                    }
                    hotSearch.append("<span style='margin: 5px;'>热搜推荐:</span>");
                }
                hotSearch.append("<button style='border-radius: 20px;margin: 5px;' class='btn btn-warning' type='button' onclick='hotSearch()'>换一批</button>");
                for (var i = 0; i < data['hotGoods'].length; i++) {
                    hotSearch.append("<button style='border-radius: 20px;margin: 5px;' class='btn btn-default' type='button' onclick='onclickHotSearch(this)'>" + data['hotGoods'][i]['sort'] + "</button>");
                }
                var nowDate = getReadMonth(new Date().getTime());
                var popularTodayDate = localStorage.getItem('popularTodayDate');
                if (popularTodayDate !== nowDate) {
                    localStorage.setItem('popularTodayDate', nowDate);
                    var popularTodayDiv = $('#popularTodayDiv');
                    popularTodayDiv.html('');
                    for (var i1 = 0; i1 < data['hotGoods'].length; i1++) {
                        if (i1 % 2 !== 0) {
                            popularTodayDiv.append("<table class='table table-condensed' style='margin-bottom: 0;word-break: break-all;'><tr><td rowspan='2' class='text-center' style='background-color: #CDFCDC;' width='25%'>热门商品</td><td class='text-left' style='font-weight: bold;'>" + data['hotGoods'][i1]['name'] + "&nbsp;&nbsp;&nbsp;&nbsp;<em>现价：" + data['hotGoods'][i1]['price'] + "</em></td></tr><tr><td class='text-left'><em><a href='javascript:void(0);' onclick='buyerViewGoods(\"" + data['hotGoods'][i1]['id'] + "\")'>立即查看</a></em></td></tr></table>");
                        } else {
                            popularTodayDiv.append("<table class='table table-condensed' style='margin-bottom: 0;word-break: break-all;'><tr><td class='text-left'  style='font-weight: bold;'>" + data['hotGoods'][i1]['name'] + "&nbsp;&nbsp;&nbsp;&nbsp;<em>现价：" + data['hotGoods'][i1]['price'] + "</em></td><td rowspan='2' class='text-center' style='background-color: #FFD6CF;' width='25%'>热门商品</td></tr><tr><td class='text-left'><em><a href='javascript:void(0);' onclick='buyerViewGoods(\"" + data['hotGoods'][i1]['id'] + "\")'>立即查看</a></em></td></tr></table>");
                        }
                    }
                    $('#popularTodayModal').modal('show');
                }
                refreshRecommend(data['hotGoods']);
            } else {
                hotSearch.html("<div class='text-center'><img src='img/error.svg' style='width: 31px;height: 31px;' alt='错误图片'/><span style='font-size: 1.6em;'>&nbsp;加载失败</span></div>");
            }
        },
        error: function () {
            hotSearch.html("<div class='text-center'><img src='img/error.svg' style='width: 31px;height: 31px;' alt='错误图片'/><span style='font-size: 1.6em;'>&nbsp;加载失败</span></div>");
        }
    });
}

//发送聊天方法
function sendMessage() {
    var regular = new RegExp(/^[\s]+$/);
    var messageBtn = $('#message');
    var message = messageBtn.val();
    if (regular.test(message) || message === '' || message === null) {
        return;
    }
    acceptMessage('user', message);
    messageBtn.val('');
    if (message === '登录' || message === '登陆' || message === '注册') {
        if (userStatus === 'offline') {
            if (message === '登录' || message === '登陆') {
                $("[href='#userLoginTab']").tab('show');
            }
            if (message === '注册') {
                $("[href='#userRegTab']").tab('show');
            }
            var userLoginAndRegModal = $('#userLoginAndRegModal');
            userLoginAndRegModal.on('shown.bs.modal', function () {
                if (message === '登录' || message === '登陆') {
                    $('#loginUserEmail').focus();
                }
                if (message === '注册') {
                    $('#regUserEmail').focus();
                }
            });
            userLoginAndRegModal.modal('show');
        } else {
            acceptMessage('robot', showUsername + '，你已经登录啦！');
        }
        return;
    }
    if (message === '注销') {
        if (userStatus === 'offline') {
            acceptMessage('robot', '游客先生，你并没有登录！');
        } else {
            userSignOut();
        }
        return;
    }
    toServerMessage(message);
}

//向服务器发送聊天消息
function toServerMessage(message) {
    $.ajax({
        url: SA + 'chatController/chat.do',
        data: {
            message: message,
            sellerId: getQueryString('sellerId')
        },
        dataType: 'json',
        success: function (data) {
            if (data['type'] === 'chat') {
                acceptMessage('robot', data['reply']);
            } else if (data['type'] === 'select') {
                acceptMessage('robot', '请稍后，小明正在帮你找关于\"' + data['reply'] + '\"的商品');
                selectLikeGoods(data['reply']);
            } else {
                acceptMessage('robot', '哎，好像出错了。小明接收不到你的消息哦。');
            }
        },
        error: function () {
            acceptMessage('robot', '哎，好像出错了。小明接收不到你的消息哦。');
        }
    });
}

//单击热搜动作
function onclickHotSearch(tag) {
    $('#message').val(tag.innerHTML);
    sendMessage();
}

//登录注册模态框关闭事件
$('#userLoginAndRegModal').on('hidden.bs.modal', function () {
    $('#loginErrorMessage').html('欢迎登录');
    $('#regErrorMessage').html('欢迎注册');
    $("[href='#userLoginTab']").tab('show');
    $(':input', '#userLoginAndRegForm').val('');
    $('#identityBtn').html("请选择&nbsp;<span class='caret'></span>");
});

//监听注册输入框
function monitorRegInput(event, source) {
    if (event.which === 13) {
        if (source.id === 'regUserEmail') {
            setTimeout('getVCode()', 0);
        }
        if (source.id === 'vCode') {
            $('#regUserUsername').focus();
        }
        if (source.id === 'regUserUsername') {
            $('#regUserPassword').focus();
        }
        if (source.id === 'regUserPassword') {
            $('#identityBtn').click();
            $('#selectIdentitySelect').focus();
        }
    }
}

//获取注册验证码
function getVCode() {
    var regUserEmailDom = $('#regUserEmail');
    regUserEmailDom.val(regUserEmailDom.val().trim());
    var regUserEmail = regUserEmailDom.val();
    if (regUserEmail === '') {
        $('#regErrorMessage').html('请输入注册账号');
        return;
    }
    $('#getVCodeBtn').html("<img src='img/loading.svg' class='loading' alt='加载图片'/><span>&nbsp;请稍后..</span>").attr('disabled', 'disabled');
    $.ajax({
        url: SA + 'userController/getVCode.do',
        data: {
            email: regUserEmail
        },
        dataType: 'json',
        success: function (data) {
            if (data['status'] === 'success') {
                $('#regErrorMessage').html('请输入验证码');
                $('#vCode').focus();
                getVCodeTimeout(60);
                return;
            } else if (data['status'] === 'hasLanded') {
                $('#regErrorMessage').html('请先退出当前用户');
            } else if (data['status'] === 'emailNotAvailable') {
                $('#regErrorMessage').html('此账号不可使用');
            } else if (data['status'] === 'mailboxAlreadyRegistered') {
                $('#regErrorMessage').html('该账号已被注册');
            } else if (data['status'] === 'emailFormatIncorrect') {
                $('#regErrorMessage').html('请输入正确的账号');
            } else if (data['status'] === 'repeatOperation') {
                $('#regErrorMessage').html('稍后才可以重新获取');
                $('#vCode').focus();
                getVCodeTimeout(parseInt(data['timeLeft']));
                return;
            } else {
                $('#regErrorMessage').html("<img src='img/error.svg' style='width: 21px;height: 21px;' alt='错误图片'/><span>&nbsp;获取验证码失败</span>");
            }
            $('#getVCodeBtn').html('获取验证码').removeAttr('disabled');
        },
        error: function () {
            $('#regErrorMessage').html("<img src='img/error.svg' style='width: 21px;height: 21px;' alt='错误图片'/><span>&nbsp;获取验证码失败</span>");
            $('#getVCodeBtn').html('获取验证码').removeAttr('disabled');
        }
    });
}

//获取注册验证码倒计时
function getVCodeTimeout(timeout) {
    if (timeout < 1) {
        $('#getVCodeBtn').html('获取验证码').removeAttr('disabled');
        return;
    } else if (timeout >= 1 && timeout < 10) {
        $('#getVCodeBtn').html('&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;' + 0 + '' + timeout + '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;');
    } else {
        $('#getVCodeBtn').html('&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;' + timeout + '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;');
    }
    setTimeout('getVCodeTimeout(' + (--timeout) + ')', 1000);
}

//用户注册选择身份
function selectIdentity(identity) {
    if (identity === 'select') {
        $('#identityBtn').html("请选择&nbsp;<span class='caret'></span>");
        $('#regUserRole').val('');
    }
    if (identity === 'merchant') {
        $('#identityBtn').html('商户&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;');
        $('#regUserRole').val('商户');
    }
    if (identity === 'customer') {
        $('#identityBtn').html('顾客&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;');
        $('#regUserRole').val('顾客');
    }
    $('#regBtn').focus();
}

//用户注册方法
function userReg() {
    var regUserEmail = $('#regUserEmail').val();
    var vCode = $('#vCode').val();
    var regUserUsername = $('#regUserUsername').val();
    var regUserPassword = $('#regUserPassword').val();
    var regUserRole = $('#regUserRole').val();
    var regErrorMessage = $('#regErrorMessage');
    if (regUserEmail === '' || vCode === '' || regUserUsername === '' || regUserPassword === '' || !(regUserRole === '顾客' || regUserRole === '商户')) {
        regErrorMessage.html('请将注册信息输入完整');
        return;
    }
    regErrorMessage.html("<img src='img/loading.svg' class='loading' alt='加载图片'/><span>&nbsp;请稍后</span>");
    $('#regBtn').attr('disabled', 'disabled');
    $.ajax({
        url: SA + 'userController/userRegistered.do',
        data: {
            email: regUserEmail,
            vCode: vCode,
            username: regUserUsername,
            password: regUserPassword,
            role: regUserRole
        },
        dataType: 'text',
        success: function (data) {
            if (data === 'success') {
                $('#userLoginAndRegModal').modal('hide');
                initPage();
            } else if (data === 'hasLanded') {
                regErrorMessage.html('请先退出当前用户');
            } else if (data === 'identityNotCorrect') {
                regErrorMessage.html('请选择正确的身份');
            } else if (data === 'passwordFormatIncorrect') {
                regErrorMessage.html('密码只能使用6-32位数字、字母、下划线');
            } else if (data === 'usernameFormatIncorrect') {
                regErrorMessage.html('昵称只能使用中文、数字、字母、下划线');
            } else if (data === 'incorrectVerificationCode') {
                regErrorMessage.html('注册验证码不正确');
            } else if (data === 'notGetVCode') {
                regErrorMessage.html('请先获取验证码');
            } else {
                regErrorMessage.html("<img src='img/error.svg' style='width: 21px;height: 21px;' alt='错误图片'/><span>&nbsp;发生错误，请重试</span>");
            }
            $('#regBtn').removeAttr('disabled');
        },
        error: function () {
            regErrorMessage.html("<img src='img/error.svg' style='width: 21px;height: 21px;' alt='错误图片'/><span>&nbsp;注册失败，请重试</span>");
            $('#regBtn').removeAttr('disabled');
        }
    });
}

//监听登录输入框
function monitorLoginInput(event, source) {
    if (event.which === 13) {
        if (source.id === 'loginUserEmail') {
            setTimeout("$('#loginUserPassword').focus()", 0);
        } else {
            userLogin();
        }
    }
}

//用户登录方法
function userLogin() {
    var loginUserEmailDom = $('#loginUserEmail');
    loginUserEmailDom.val(loginUserEmailDom.val().trim());
    var loginUserEmail = loginUserEmailDom.val();
    var loginUserPassword = $('#loginUserPassword').val();
    var loginErrorMessage = $('#loginErrorMessage');
    if (loginUserEmail === '' || loginUserPassword === '') {
        loginErrorMessage.html('请将登录信息输入完整');
        return;
    }
    $('#loginBtn').attr('disabled', 'disabled');
    loginErrorMessage.html("<img src='img/loading.svg' class='loading' alt='加载图片'/><span>&nbsp;请稍后</span>");
    $.ajax({
        url: SA + 'userController/userLogin.do',
        data: {
            email: loginUserEmail,
            password: loginUserPassword
        },
        dataType: 'text',
        success: function (data) {
            if (data === 'success') {
                localStorage.setItem('email', loginUserEmail);
                localStorage.setItem('password', loginUserPassword);
                $('#userLoginAndRegModal').modal('hide');
                initPage();
            } else if (data === 'passwordError') {
                loginErrorMessage.html('密码错误，请重试');
            } else if (data === 'emailNotExist') {
                loginErrorMessage.html('该账号尚未注册');
            } else if (data === 'emailError') {
                loginErrorMessage.html('登录账号不正确');
            } else {
                loginErrorMessage.html("<img src='img/error.svg' style='width: 21px;height: 21px;' alt='错误图片'/><span>&nbsp;发生错误，请重试</span>");
            }
            $('#loginBtn').removeAttr('disabled');
        },
        error: function () {
            loginErrorMessage.html("<img src='img/error.svg' style='width: 21px;height: 21px;' alt='错误图片'/><span>&nbsp;登录失败，请重试</span>");
            $('#loginBtn').removeAttr('disabled');
        }
    });
}

//用户注销方法
function userSignOut() {
    $.ajax({
        url: SA + 'userController/userSignOut.do',
        dataType: 'text',
        success: function (data) {
            if (data === 'NoLogin') {
                acceptMessage('robot', '游客先生，你并没有登录！');
            } else if (data === 'success') {
                acceptMessage('robot', '再见啦。');
                localStorage.removeItem('email');
                localStorage.removeItem('password');
                initPage();
            } else {
                acceptMessage('robot', '哎，好像出错了。小明接收不到你的消息哦。');
            }
        },
        error: function () {
            acceptMessage('robot', '哎，好像出错了。小明接收不到你的消息哦。');
        }
    });
}

//用户点击语音事件
function openVoice() {
    window.ov.openVoice();
}

//图片搜索
function chooseImage(images) {
    var imageSearchBtn = $('#imageSearchBtn');
    imageSearchBtn.attr('style', "background: #F0AD4E url('img/load.svg') no-repeat center;").attr('disabled', 'disabled');
    try {
        lrz(images.files[0], {width: 240, quality: 0.9}).then(function (rst) {
            acceptMessage('user', "<img src='" + rst['base64'] + "' alt='聊天图片'/>");
            var base64 = rst['base64'];
            base64 = base64.substring(base64.indexOf(',') + 1, base64.length);
            $.ajax({
                url: SA + 'chatController/searchImage.do',
                data: {
                    image: base64
                },
                dataType: 'text',
                success: function (data) {
                    if (data !== '') {
                        acceptMessage('robot', '请稍后，小明正在帮你找关于\"' + data + '\"的商品');
                        selectLikeGoods(data);
                    } else {
                        acceptMessage('robot', '小明看不清这是什么啦。');
                    }
                    imageSearchBtn.attr('style', "background: #F0AD4E url('img/image.svg') no-repeat center;").removeAttr('disabled');
                },
                error: function () {
                    imageSearchBtn.attr('style', "background: #F0AD4E url('img/image.svg') no-repeat center;").removeAttr('disabled');
                    acceptMessage('robot', '换张图片？或者看看你的网络有问题没。');
                }
            });
        }).catch(function () {
            imageSearchBtn.attr('style', "background: #F0AD4E url('img/image.svg') no-repeat center;").removeAttr('disabled');
            acceptMessage('robot', '你的图片怕是有问题哦。');
        });
    } catch (error) {
        imageSearchBtn.attr('style', "background: #F0AD4E url('img/image.svg') no-repeat center;").removeAttr('disabled');
    }
}

//JS控制发送消息
function autoSendMessage(message) {
    $('#message').val(message);
    sendMessage();
}

//打开功能列表时事件
function openFeaturesList() {
    $('#featuresListModal').modal('show');
    if (userStatus === 'online') {
        hotSearch();
        refreshSubscription('first');
        $('#subscriptionPageNav').removeClass('hidden');
        refreshHistory('first');
        $('#historyPageNav').removeClass('hidden');
    } else {
        $('#subscriptionTabValue').html("<img src='img/error.svg' style='width: 42px;height: 42px;' alt='错误图片'/><br/><span style='font-size: 2em;'><a href='javascript:void(0);' data-toggle='modal' data-target='#userLoginAndRegModal' data-dismiss='modal'>点此登录或注册</a></span>");
        $('#historyTabValue').html("<img src='img/error.svg' style='width: 42px;height: 42px;' alt='错误图片'/><br/><span style='font-size: 2em;'><a href='javascript:void(0);' data-toggle='modal' data-target='#userLoginAndRegModal' data-dismiss='modal'>点此登录或注册</a></span>");
        $('#subscriptionPageNav').addClass('hidden');
        $('#historyPageNav').addClass('hidden');
    }
}

//用户查看推荐
function refreshRecommend(hotGoods) {
    var recommendTabValue = $('#recommendTabValue');
    recommendTabValue.html("<img src='img/loading.svg' class='loading' style='width: 42px;height: 42px;' alt='加载图片'/><br/><span style='font-size: 2em;'>请稍后</span>");
    $.ajax({
        url: SA + 'featuresController/recommend.do',
        dataType: 'json',
        success: function (data) {
            if (data['status'] === 'offline') {
                recommendTabValue.html("<h3><a href='javascript:void(0);' data-toggle='modal' data-target='#userLoginAndRegModal' data-dismiss='modal'>登录后小明会更懂你哦</a></h3>");
            }
            if (data['status'] === 'null') {
                recommendTabValue.html('<h3>勤搜索小明才能更懂你哦</h3>');
            }
            if (data['status'] === 'offline' || data['status'] === 'null') {
                for (var i = 0; i < hotGoods.length; i++) {
                    if (i % 2 !== 0) {
                        recommendTabValue.append("<table class='table table-condensed' style='margin-bottom: 0;word-break: break-all;'><tr><td rowspan='2' class='text-center' style='background-color: #CDFCDC;' width='25%'>热门商品</td><td class='text-left' style='font-weight: bold;'>" + hotGoods[i]['name'] + "&nbsp;&nbsp;&nbsp;&nbsp;<em>现价：" + hotGoods[i]['price'] + "</em></td></tr><tr><td class='text-left'><em><a href='javascript:void(0);' onclick='buyerViewGoods(\"" + hotGoods[i]['id'] + "\")'>立即查看</a></em></td></tr></table>");
                    } else {
                        recommendTabValue.append("<table class='table table-condensed' style='margin-bottom: 0;word-break: break-all;'><tr><td class='text-left'  style='font-weight: bold;'>" + hotGoods[i]['name'] + "&nbsp;&nbsp;&nbsp;&nbsp;<em>现价：" + hotGoods[i]['price'] + "</em></td><td rowspan='2' class='text-center' style='background-color: #FFD6CF;' width='25%'>热门商品</td></tr><tr><td class='text-left'><em><a href='javascript:void(0);' onclick='buyerViewGoods(\"" + hotGoods[i]['id'] + "\")'>立即查看</a></em></td></tr></table>");
                    }
                }
            } else if (data['status'] === 'success') {
                recommendTabValue.html('');
                for (var i1 = 0; i1 < hotGoods.length; i1++) {
                    if (i1 % 2 === 0) {
                        recommendTabValue.append("<table class='table table-condensed' style='margin-bottom: 0;word-break: break-all;'><tr><td class='text-left' style='font-weight: bold;'>" + data['goods'][i1 / 2]['name'] + "&nbsp;&nbsp;&nbsp;&nbsp;<em>现价：" + data['goods'][i1 / 2]['price'] + "</em></td><td rowspan='2' class='text-center' style='background-color: #FFD6CF;' width='25%'>智能推荐</td></tr><tr><td class='text-left'><em><a href='javascript:void(0);' onclick='buyerViewGoods(\"" + data['goods'][i1 / 2]['id'] + "\")'>立即查看</a></em></td></tr></table>");
                    } else {
                        recommendTabValue.append("<table class='table table-condensed' style='margin-bottom: 0;word-break: break-all;'><tr><td rowspan='2' class='text-center' style='background-color: #CDFCDC;' width='25%'>热门商品</td><td class='text-left' style='font-weight: bold;'>" + hotGoods[i1]['name'] + "&nbsp;&nbsp;&nbsp;&nbsp;<em>现价：" + hotGoods[i1]['price'] + "</em></td></tr><tr><td class='text-left'><em><a href='javascript:void(0);' onclick='buyerViewGoods(\"" + hotGoods[i1]['id'] + "\")'>立即查看</a></em></td></tr></table>");

                    }
                }
            } else {
                recommendTabValue.html("<img src='img/error.svg' style='width: 42px;height: 42px;' alt='错误图片'/><br/><span style='font-size: 2em;'>请再次打开以重试</span>");
            }
            if (data['status'] === 'offline' || data['status'] === 'null' || data['status'] === 'success') {
                recommendTabValue.append("<button style='margin-top: 10px;' class='btn btn-warning btn-block' type='button' onclick='hotSearch()'>换一批</button>");
            }
        },
        error: function () {
            recommendTabValue.html("<img src='img/error.svg' style='width: 42px;height: 42px;' alt='错误图片'/><br/><span style='font-size: 2em;'>请再次打开以重试</span>");
        }
    });
}

//查询用户收藏内容
function refreshSubscription(action) {
    var subscriptionPage = $('#subscriptionPage');
    var subscriptionLastPage = $('#subscriptionLastPage');
    var selectPage = parseInt(subscriptionPage.html());
    var lastPage = parseInt(subscriptionLastPage.html());
    if (action === 'first') {
        selectPage = 1;
    }
    if (action === 'previous' && selectPage > 1) {
        selectPage--;
    }
    if (action === 'next' && selectPage < lastPage) {
        selectPage++;
    }
    if (action === 'last') {
        selectPage = lastPage;
    }
    subscriptionPage.html('1');
    subscriptionLastPage.html('1');
    var subscriptionTabValue = $('#subscriptionTabValue');
    subscriptionTabValue.html("<img src='img/loading.svg' class='loading' style='width: 42px;height: 42px;' alt='加载图片'/><br/><span style='font-size: 2em;'>请稍后</span>");
    $.ajax({
        url: SA + 'featuresController/selectSubscription.do',
        data: {
            page: selectPage
        },
        dataType: 'json',
        success: function (data) {
            if (parseInt(data['pageNum']) > 0) {
                subscriptionPage.html(selectPage);
                subscriptionLastPage.html(data['pageNum']);
                subscriptionTabValue.html('');
                if (!jQuery.isEmptyObject(data['subscription'])) {
                    for (var i = 0; i < data['subscription'].length; i++) {
                        if (data['subscription'][i]['type'] === 'select') {
                            subscriptionTabValue.append("<table class='table table-condensed' style='margin-bottom: 0;word-break: break-all;'><tr><td rowspan='2' class='text-center' style='background-color: #FF9E9E;' width='25%'>查询<td class='text-left' style='font-weight: bold;'>内容：" + data['subscription'][i]['content'] + "</td></tr><tr></td><td class='text-left'><em><a href='javascript:void(0);' onclick='againSelect(\"" + data['subscription'][i]['content'] + "\")'>再次查询</a></em></td></tr></table>");
                        } else {
                            subscriptionTabValue.append("<table class='table table-condensed' style='margin-bottom: 0;word-break: break-all;'><tr><td class='text-left' style='font-weight: bold;'>" + data['subscription'][i]['goodsName'] + "</td><td rowspan='2' class='text-center' style='background-color: #9CD8F0;' width='25%'>商品</td></tr><tr><td class='text-left'><em><a href='javascript:void(0);' onclick='buyerViewGoods(\"" + data['subscription'][i]['content'] + "\")'>立即查看</a></em></td></tr></table>");
                        }
                    }
                } else {
                    refreshSubscription('first');
                }
            } else if (parseInt(data['pageNum']) === 0) {
                subscriptionTabValue.html("<img src='img/error.svg' style='width: 42px;height: 42px;' alt='错误图片'/><br/><span style='font-size: 2em;'>暂无，快去收藏吧！</span>");
            } else {
                subscriptionTabValue.html("<img src='img/error.svg' style='width: 42px;height: 42px;' alt='错误图片'/><br/><span style='font-size: 2em;'>请再次打开以重试</span>");
            }
        },
        error: function () {
            subscriptionTabValue.html("<img src='img/error.svg' style='width: 42px;height: 42px;' alt='错误图片'/><br/><span style='font-size: 2em;'>请再次打开以重试</span>");
        },
        complete: function () {
            if (parseInt(subscriptionPage.html()) === 1) {
                $('#subscriptionPreviousPageLi').addClass('disabled');
                $('#subscriptionFirstPageLi').addClass('disabled');
            } else {
                $('#subscriptionPreviousPageLi').removeClass('disabled');
                $('#subscriptionFirstPageLi').removeClass('disabled');
            }
            if (parseInt(subscriptionPage.html()) === parseInt(subscriptionLastPage.html())) {
                $('#subscriptionLastPageLi').addClass('disabled');
                $('#subscriptionNextPageLi').addClass('disabled');
            } else {
                $('#subscriptionLastPageLi').removeClass('disabled');
                $('#subscriptionNextPageLi').removeClass('disabled');
            }
        }
    });
}

//用户再次查询收藏内容
function againSelect(content) {
    $('#featuresListModal').modal('hide');
    autoSendMessage(content);
}

//查询用户聊天记录
function refreshHistory(action) {
    var historyPage = $('#historyPage');
    var historyLastPage = $('#historyLastPage');
    var selectPage = parseInt(historyPage.html());
    var lastPage = parseInt(historyLastPage.html());
    if (action === 'first') {
        selectPage = 1;
    }
    if (action === 'previous' && selectPage > 1) {
        selectPage--;
    }
    if (action === 'next' && selectPage < lastPage) {
        selectPage++;
    }
    if (action === 'last') {
        selectPage = lastPage;
    }
    historyPage.html('1');
    historyLastPage.html('1');
    var historyTabValue = $('#historyTabValue');
    historyTabValue.html("<img src='img/loading.svg' class='loading' style='width: 42px;height: 42px;' alt='加载图片'/><br/><span style='font-size: 2em;'>请稍后</span>");
    $.ajax({
        url: SA + 'featuresController/selectHistory.do',
        data: {
            page: selectPage
        },
        dataType: 'json',
        success: function (data) {
            if (parseInt(data['pageNum']) > 0) {
                historyPage.html(selectPage);
                historyLastPage.html(data['pageNum']);
                historyTabValue.html('');
                if (!jQuery.isEmptyObject(data['history'])) {
                    for (var i = 0; i < data['history'].length; i++) {
                        if (data['history'][i]['sender'] === 'bot') {
                            historyTabValue.append("<table class='table table-condensed' style='margin-bottom: 0;word-break: break-all;'><tr><td rowspan='2' class='text-center' style='background-color: #F8F9C2;' width='25%'>小明</td><td class='text-left'><em>" + getReadTime(data['history'][i]['time']) + "</em></td></tr><tr><td class='text-left' style='font-weight: bold;'>" + data['history'][i]['content'] + "</td></tr></table>");
                        } else {
                            historyTabValue.append("<table class='table table-condensed' style='margin-bottom: 0;word-break: break-all;'><tr><td class='text-left'><em>" + getReadTime(data['history'][i]['time']) + "</em></td><td rowspan='2' class='text-center' style='background-color: #C4F2EF;' width='25%'>" + showUsername + "</td></tr><tr><td class='text-left' style='font-weight: bold;'>" + data['history'][i]['content'] + "</td></tr></table>");
                        }
                    }
                } else {
                    refreshHistory('first');
                }
            } else if (parseInt(data['pageNum']) === 0) {
                historyTabValue.html("<img src='img/error.svg' style='width: 42px;height: 42px;' alt='错误图片'/><br/><span style='font-size: 2em;'>暂无，快去聊天吧！</span>");
            } else {
                historyTabValue.html("<img src='img/error.svg' style='width: 42px;height: 42px;' alt='错误图片'/><br/><span style='font-size: 2em;'>请再次打开以重试</span>");
            }
        },
        error: function () {
            historyTabValue.html("<img src='img/error.svg' style='width: 42px;height: 42px;' alt='错误图片'/><br/><span style='font-size: 2em;'>请再次打开以重试</span>");
        },
        complete: function () {
            if (parseInt(historyPage.html()) === 1) {
                $('#historyPreviousPageLi').addClass('disabled');
                $('#historyFirstPageLi').addClass('disabled');
            } else {
                $('#historyPreviousPageLi').removeClass('disabled');
                $('#historyFirstPageLi').removeClass('disabled');
            }
            if (parseInt(historyPage.html()) === parseInt(historyLastPage.html())) {
                $('#historyLastPageLi').addClass('disabled');
                $('#historyNextPageLi').addClass('disabled');
            } else {
                $('#historyLastPageLi').removeClass('disabled');
                $('#historyNextPageLi').removeClass('disabled');
            }
        }
    });
}

//打开后台管理时事件
function openAdminManage() {
    $('#adminModal').modal('show');
    adminRefreshStatistics();
    adminRefreshGoods('first');
    adminHeadOffSetup();
    richTextEditorSetup();
}

//更新统计信息
function adminRefreshStatistics() {
    var statisticsDiv = $('#statisticsDiv');
    statisticsDiv.html("<img src='img/loading.svg' class='loading' style='width: 42px;height: 42px;' alt='加载图片'/><br/><span style='font-size: 2em;'>请稍后</span>");
    $.ajax({
        url: SA + 'adminController/statistics.do',
        dataType: 'json',
        success: function (data) {
            if (!jQuery.isEmptyObject(data)) {
                statisticsDiv.html("<div id='accessAmount'></div><div id='heatAmount'></div>");
                if (!jQuery.isEmptyObject(data['weekAmount'])) {
                    var amountNum = {};
                    for (var i = 0; i < 7; i++) {
                        amountNum[i] = jQuery.isEmptyObject(data['weekAmount'][i]) ? 0 : data['weekAmount'][i]['amount'];
                    }
                    var timestamp = new Date().getTime();
                    $('#accessAmount').highcharts({
                        chart: {
                            type: 'line'
                        },
                        title: {
                            text: '访问统计图'
                        },
                        xAxis: {
                            title: {
                                text: '日期'
                            },
                            categories: [getReadMonth(timestamp - 518400000), getReadMonth(timestamp - 432000000), getReadMonth(timestamp - 345600000), getReadMonth(timestamp - 259200000), getReadMonth(timestamp - 172800000), getReadMonth(timestamp - 86400000), getReadMonth(timestamp)]
                        },
                        yAxis: {
                            title: {
                                text: '人数 (位)'
                            }
                        },
                        credits: {
                            text: '小明智能导购机器人',
                            href: 'https://www.spicybar.cn/ChatRobot/'
                        },
                        series: [{
                            name: showUsername,
                            data: [amountNum[6], amountNum[5], amountNum[4], amountNum[3], amountNum[2], amountNum[1], amountNum[0]]
                        }]
                    });
                }
                if (!jQuery.isEmptyObject(data['hotSearch'])) {
                    var hotGoods = {};
                    for (var i1 = 0; i1 < 7; i1++) {
                        if (jQuery.isEmptyObject(data['hotSearch'][i1])) {
                            hotGoods[i1] = {};
                            hotGoods[i1]['name'] = '空';
                            hotGoods[i1]['heat'] = 0;
                        } else {
                            hotGoods[i1] = {};
                            hotGoods[i1]['name'] = data['hotSearch'][i1]['name'];
                            hotGoods[i1]['heat'] = data['hotSearch'][i1]['heat'];
                        }
                    }
                    $('#heatAmount').highcharts({
                        chart: {
                            type: 'bar'
                        },
                        title: {
                            text: '商品热度图'
                        },
                        xAxis: {
                            title: {
                                text: '商品'
                            },
                            categories: [hotGoods[0]['name'], hotGoods[1]['name'], hotGoods[2]['name'], hotGoods[3]['name'], hotGoods[4]['name'], hotGoods[5]['name'], hotGoods[6]['name']]
                        },
                        yAxis: {
                            title: {
                                text: '热量 (度)'
                            }
                        },
                        credits: {
                            text: '小明智能导购机器人',
                            href: 'https://www.spicybar.cn/ChatRobot/'
                        },
                        series: [{
                            name: showUsername,
                            data: [hotGoods[0]['heat'], hotGoods[1]['heat'], hotGoods[2]['heat'], hotGoods[3]['heat'], hotGoods[4]['heat'], hotGoods[5]['heat'], hotGoods[6]['heat']]
                        }]
                    });
                }
                if (jQuery.isEmptyObject(data['hotSearch']) && jQuery.isEmptyObject(data['weekAmount'])) {
                    statisticsDiv.html("<img src='img/error.svg' style='width: 42px;height: 42px;' alt='错误图片'/><br/><span style='font-size: 2em;'>暂时还没有统计信息哦</span>");
                }
            } else {
                statisticsDiv.html("<img src='img/error.svg' style='width: 42px;height: 42px;' alt='错误图片'/><br/><span style='font-size: 2em;'>暂时还没有统计信息哦</span>");
            }
        }
        ,
        error: function () {
            statisticsDiv.html("<img src='img/error.svg' style='width: 42px;height: 42px;' alt='错误图片'/><br/><span style='font-size: 2em;'>请再次打开以重试</span>");
        }
    });
}

//商品列表
var goodsMap = {};

//更新商品列表
function adminRefreshGoods(action) {
    var goodsPage = $('#goodsPage');
    var goodsLastPage = $('#goodsLastPage');
    var selectPage = parseInt(goodsPage.html());
    var lastPage = parseInt(goodsLastPage.html());
    if (action === 'first') {
        selectPage = 1;
    }
    if (action === 'previous' && selectPage > 1) {
        selectPage--;
    }
    if (action === 'next' && selectPage < lastPage) {
        selectPage++;
    }
    if (action === 'last') {
        selectPage = lastPage;
    }
    goodsPage.html('1');
    goodsLastPage.html('1');
    var goodsManageDiv = $('#goodsManageDiv');
    goodsManageDiv.html("<img src='img/loading.svg' class='loading' style='width: 42px;height: 42px;' alt='加载图片'/><br/><span style='font-size: 2em;'>请稍后</span>");
    $.ajax({
        url: SA + 'adminController/selectGoods.do',
        data: {
            page: selectPage
        },
        dataType: 'json',
        success: function (data) {
            if (parseInt(data['pageNum']) > 0) {
                goodsPage.html(selectPage);
                goodsLastPage.html(data['pageNum']);
                goodsManageDiv.html("<table class='table' style='padding: 0;margin: 0;font-weight: bold'><tr><td width='20%' class='text-left'>商品名称</td><td width='20%' class='text-left'>商品价格</td><td class='text-left'>商品标签</td><td width='45px'>查看</td><td width='45px'>修改</td><td width='45px'>删除</td></tr></table>");
                goodsMap = {};
                if (!jQuery.isEmptyObject(data['goods'])) {
                    for (var i = 0; i < data['goods'].length; i++) {
                        goodsMap[data['goods'][i]['id']] = data['goods'][i]['introduction'];
                        goodsManageDiv.append("<table class='table table-hover' style='padding: 0;margin: 0;'><tr><td id='nameTd" + data['goods'][i]['id'] + "' width='20%' class='text-left'>" + data['goods'][i]['name'] + "</td><td width='20%' class='text-left text-danger'><span>&yen;</span><span id='priceTd" + data['goods'][i]['id'] + "'>" + data['goods'][i]['price'] + "</span></td><td id='sortTd" + data['goods'][i]['id'] + "' class='text-left text-muted'>" + data['goods'][i]['sort'] + "</td><td id='viewTd" + data['goods'][i]['id'] + "' width='45px' style='cursor: pointer;' onclick='buyerViewGoods(" + data['goods'][i]['id'] + ")'><img src='img/view.svg' style='height: 21px;' alt='查看图标'/></td><td id='modifyTd" + data['goods'][i]['id'] + "' width='45px' style='cursor: pointer;' onclick='sellersModifyGoods(" + data['goods'][i]['id'] + ")'><img src='img/modify.svg' style='height: 21px;' alt='修改图标'/></td><td id='removeTd" + data['goods'][i]['id'] + "' width='45px' style='cursor: pointer;' onclick='sellersRemoveGoods(" + data['goods'][i]['id'] + ")'><img src='img/remove.svg' style='height: 21px;' alt='删除图标'/></td></tr></table>");
                    }
                } else {
                    adminRefreshGoods('first');
                }
            } else if (parseInt(data['pageNum']) === 0) {
                goodsManageDiv.html("<img src='img/error.svg' style='width: 42px;height: 42px;' alt='错误图片'/><br/><span style='font-size: 2em;'>暂无商品，快添加吧</span>");
            } else {
                goodsManageDiv.html("<img src='img/error.svg' style='width: 42px;height: 42px;' alt='错误图片'/><br/><span style='font-size: 2em;'>请再次打开以重试</span>");
            }
        },
        error: function () {
            goodsManageDiv.html("<img src='img/error.svg' style='width: 42px;height: 42px;' alt='错误图片'/><br/><span style='font-size: 2em;'>请再次打开以重试</span>");
        },
        complete: function () {
            if (parseInt(goodsPage.html()) === 1) {
                $('#goodsPreviousPageLi').addClass('disabled');
                $('#goodsFirstPageLi').addClass('disabled');
            } else {
                $('#goodsPreviousPageLi').removeClass('disabled');
                $('#goodsFirstPageLi').removeClass('disabled');
            }
            if (parseInt(goodsPage.html()) === parseInt(goodsLastPage.html())) {
                $('#goodsLastPageLi').addClass('disabled');
                $('#goodsNextPageLi').addClass('disabled');
            } else {
                $('#goodsLastPageLi').removeClass('disabled');
                $('#goodsNextPageLi').removeClass('disabled');
            }
        }
    });
}

//更新拦截设置列表
function adminHeadOffSetup() {
    var headOffSetupDiv = $('#headOffSetupDiv');
    $('#addHeadOffBtn').addClass('hidden').removeAttr('disabled');
    headOffSetupDiv.html("<img src='img/loading.svg' class='loading' style='width: 42px;height: 42px;' alt='加载图片'/><br/><span style='font-size: 2em;'>请稍后</span>");
    $.ajax({
        url: SA + 'adminController/selectHeadOff.do',
        dataType: 'json',
        success: function (data) {
            headOffSetupDiv.html("<div class='alert alert-warning' role='alert'><strong id='headOffSetupMessage'>前五条设置将会显示在聊天框哦</strong></div>");
            if (!jQuery.isEmptyObject(data)) {
                headOffSetupDiv.append("<table class='table table-hover table-condensed' style='margin: 0;'><tr style='font-weight: bold;'><td width='30%'>拦截语句</td><td>拦截回复</td><td width='45px'>编辑</td><td width='45px'>删除</td></tr></table>");
                for (var i = 0; i < data.length; i++) {
                    headOffSetupDiv.append("<table class='table table-hover table-condensed' style='margin: 0;'><tr id='headOff" + i + "' style='height: 45px;'><td style='vertical-align:middle;width: 30%;'>" + data[i]['pattern'] + "</td><td style='vertical-align:middle;'>" + data[i]['template'] + "</td><td style='cursor: pointer;width: 45px;vertical-align:middle;' onclick='adminModifyHeadOff(" + i + ")'><img src='img/modify.svg' style='height: 21px;' alt='修改图标'></td><td style='cursor: pointer;width: 45px;vertical-align:middle;' onclick='adminRemoveHeadOff(" + i + ")'><img src='img/remove.svg' style='height: 21px;' alt='删除图标'></td></tr></table>");
                }
            } else {
                headOffSetupDiv.append("<img src='img/error.svg' style='width: 42px;height: 42px;' alt='错误图片'/><br/><span style='font-size: 2em;'>您还没有设置哦</span>");
            }
            $('#addHeadOffBtn').removeClass('hidden');
        },
        error: function () {
            headOffSetupDiv.html("<img src='img/error.svg' style='width: 42px;height: 42px;' alt='错误图片'/><br/><span style='font-size: 2em;'>请再次打开以重试</span>");
        }
    });
}

//删除拦截
function adminRemoveHeadOff(id) {
    var td = $('#headOff' + id + ' td');
    if ($(td[3]).css('cursor') === 'no-drop') {
        return;
    }
    var statement = $(td[0]).html();
    $('#headOff' + id).parent().parent().remove();
    $.ajax({
        url: SA + 'adminController/removeHeadOff.do',
        data: {
            statement: statement
        },
        dataType: 'text',
        success: function (data) {
            if (data === 'success') {
                $('#headOffSetupMessage').html('前五条设置将会显示在聊天框哦');
            } else {
                $('#headOffSetupMessage').html('删除失败，请重试');
            }
        },
        error: function () {
            $('#headOffSetupMessage').html('删除失败，请重试');
        }
    });
}

//新建拦截
function adminAddHeadOff() {
    $('#headOffSetupDiv').append("<table class='table table-hover table-condensed' style='margin: 0;'><tr style='height: 45px;'><td style='vertical-align:middle;width: 30%;'><input id='newStatement' class='form-control' placeholder='拦截语句'/></td><td style='vertical-align:middle;'><input id='newReply' class='form-control' placeholder='拦截回复'/></td><td style='cursor: pointer;width: 45px;vertical-align:middle;' onclick='adminAddHeadOffOver()'><img src='img/complete.svg' style='height: 21px;' alt='完成图标'></td></tr></table>");
    $('#addHeadOffBtn').attr('disabled', 'disabled');
}

//新建拦截完成
function adminAddHeadOffOver() {
    var statement = $('#newStatement').val();
    var reply = $('#newReply').val();
    if (statement === '' || reply === '') {
        $('#headOffSetupMessage').html('请输入完整');
        return;
    }
    $.ajax({
        url: SA + 'adminController/addHeadOff.do',
        data: {
            statement: statement,
            reply: reply
        },
        dataType: 'text',
        success: function (data) {
            if (data === 'success') {
                $('#headOffSetupMessage').html('前五条设置将会显示在聊天框哦');
                adminHeadOffSetup();
            } else if (data === 'null') {
                $('#headOffSetupMessage').html('请输入完整');
            } else if (data === 'exist') {
                $('#headOffSetupMessage').html('已经存在相同拦截');
            } else if (data === 'illegal') {
                $('#headOffSetupMessage').html('这个不行哦，换一个拦截词呗');
            } else {
                $('#headOffSetupMessage').html('修改失败，请重试');
            }
        },
        error: function () {
            $('#headOffSetupMessage').html('新增失败，请重试');
        }
    });
}

//修改拦截
function adminModifyHeadOff(id) {
    var td = $('#headOff' + id + ' td');
    $(td[3]).css('cursor', 'no-drop');
    var statement = $(td[0]);
    statement.html("<input class='form-control' placeholder='拦截语句' data-originalStatement='" + statement.html() + "' value='" + statement.html() + "'/>");
    var reply = $(td[1]);
    reply.html("<input class='form-control' placeholder='拦截回复' value='" + reply.html() + "'/>");
    var editIcon = $(td[2]);
    editIcon.attr('onclick', 'adminModifyHeadOffOver(' + id + ')');
    editIcon.html("<img src='img/complete.svg' style='height: 21px;' alt='完成图标'>");
}

//修改拦截完成
function adminModifyHeadOffOver(id) {
    var input = $('#headOff' + id + ' input');
    var newStatementValue = $(input[0]).val();
    var newReplyValue = $(input[1]).val();
    if (newStatementValue === '' || newReplyValue === '') {
        $('#headOffSetupMessage').html('请输入完整');
        return;
    }
    var td = $('#headOff' + id + ' td');
    $(td[0]).html(newStatementValue);
    $(td[1]).html(newReplyValue);
    var editIcon = $(td[2]);
    editIcon.attr('onclick', 'adminModifyHeadOff(' + id + ')');
    editIcon.html("<img src='img/modify.svg' style='height: 21px;' alt='修改图标'>");
    $.ajax({
        url: SA + 'adminController/modifyHeadOff.do',
        data: {
            newStatement: newStatementValue,
            newReply: newReplyValue,
            originalStatement: $(input[0]).attr('data-originalStatement')
        },
        dataType: 'text',
        success: function (data) {
            if (data === 'success') {
                $('#headOffSetupMessage').html('前五条设置将会显示在聊天框哦');
                $(td[3]).css('cursor', 'pointer');
            } else if (data === 'null') {
                $('#headOffSetupMessage').html('请输入完整');
            } else if (data === 'illegal') {
                $('#headOffSetupMessage').html('这个不行哦，换一个拦截词呗');
            } else {
                $('#headOffSetupMessage').html('修改失败，请重试');
            }
        },
        error: function () {
            $('#headOffSetupMessage').html('修改失败，请重试');
        }
    });
}

//新增商品模态框关闭事件
$('#addGoodsModal').on('hidden.bs.modal', function () {
    $('#addGoodsMessage').html('请将商品信息填写完整');
    $('#goodsName').val('');
    $('#goodsPrice').val('');
    $('#goodsSort').val('');
    richTextEditor.$txt.html('<p>请在此编辑商品图文介绍...</p>');
    $('#addGoodsBtn').html('添加').attr('onclick', 'sellersAddGoods()');
});

//新增商品
function sellersAddGoods() {
    var goodsName = $('#goodsName').val();
    var goodsPrice = $('#goodsPrice').val();
    var goodsSort = $('#goodsSort').val();
    var goodsIntroduction = richTextEditor.$txt.html();
    var addGoodsMessage = $('#addGoodsMessage');
    if (goodsName === '' || goodsPrice === '' || goodsSort === '') {
        addGoodsMessage.html('请将商品信息填写完整');
        return;
    }
    addGoodsMessage.html("<img src='img/loading.svg' class='loading' alt='加载图片'/><span>&nbsp;请稍后</span>");
    $('#addGoodsBtn').attr('disabled', 'disabled');
    $.ajax({
        url: SA + 'adminController/addGoods.do',
        data: {
            name: goodsName,
            price: goodsPrice,
            sort: goodsSort,
            introduction: goodsIntroduction
        },
        dataType: 'text',
        success: function (data) {
            if (data === 'success') {
                $('#addGoodsModal').modal('hide');
                adminRefreshGoods('refresh');
            } else if (data === 'incompleteInformation') {
                addGoodsMessage.html('请将商品信息填写完整');
            } else if (data === 'priceError') {
                addGoodsMessage.html('请输入正确的价格');
            } else if (data === 'noLogin') {
                addGoodsMessage.html("<img src='img/error.svg' style='width: 21px;height: 21px;' alt='错误图片'/><span>&nbsp;您没有权限</span>");
            } else if (data === 'illegal') {
                addGoodsMessage.html("<img src='img/error.svg' style='width: 21px;height: 21px;' alt='错误图片'/><span>&nbsp;这个标签用户会搜索不到的哦</span>");
            } else {
                addGoodsMessage.html("<img src='img/error.svg' style='width: 21px;height: 21px;' alt='错误图片'/><span>&nbsp;发生错误，请重试</span>");
            }
            $('#addGoodsBtn').removeAttr('disabled');
        },
        error: function () {
            addGoodsMessage.html("<img src='img/error.svg' style='width: 21px;height: 21px;' alt='错误图片'/><span>&nbsp;发生错误，请重试</span>");
            $('#addGoodsBtn').removeAttr('disabled');
        }
    });
}

//删除商品
function sellersRemoveGoods(id) {
    var removeTd = $('#removeTd' + id);
    if (removeTd.css('cursor') === 'no-drop') {
        return;
    }
    removeTd.css('cursor', 'no-drop');
    $.ajax({
        url: SA + 'adminController/removeGoods.do',
        data: {
            id: id
        },
        dataType: 'text',
        success: function (data) {
            if (data === 'success') {
                adminRefreshGoods('refresh');
            } else {
                $('#removeTd' + id).css('cursor', 'pointer');
            }
        },
        error: function () {
            $('#removeTd' + id).css('cursor', 'pointer');
        }
    });
}

//修改商品
function sellersModifyGoods(id) {
    $('#goodsName').val($('#nameTd' + id).html());
    $('#goodsPrice').val($('#priceTd' + id).html());
    $('#goodsSort').val($('#sortTd' + id).html());
    richTextEditor.$txt.html(goodsMap[id]);
    $('#addGoodsBtn').html('修改').attr('onclick', 'sellersModifyGoodsOver(' + id + ')');
    $('#addGoodsModal').modal('show');
}

//修改商品结束事件
function sellersModifyGoodsOver(id) {
    var goodsName = $('#goodsName').val();
    var goodsPrice = $('#goodsPrice').val();
    var goodsSort = $('#goodsSort').val();
    var goodsIntroduction = richTextEditor.$txt.html();
    $.ajax({
        url: SA + 'adminController/updateGoods.do',
        data: {
            id: id,
            name: goodsName,
            price: goodsPrice,
            sort: goodsSort,
            introduction: goodsIntroduction
        },
        dataType: 'text',
        success: function (data) {
            if (data === 'success') {
                $('#addGoodsModal').modal('hide');
                adminRefreshGoods('refresh');
            } else if (data === 'incompleteInformation') {
                $('#addGoodsMessage').html('请将商品信息填写完整');
            } else if (data === 'priceError') {
                $('#addGoodsMessage').html('请输入正确的价格');
            } else if (data === 'noLogin') {
                $('#addGoodsMessage').html("<img src='img/error.svg' style='width: 21px;height: 21px;' alt='错误图片'/><span>&nbsp;您没有权限</span>");
            } else if (data === 'illegal') {
                $('#addGoodsMessage').html("<img src='img/error.svg' style='width: 21px;height: 21px;' alt='错误图片'/><span>&nbsp;这个标签用户会搜索不到的哦</span>");
            } else {
                $('#addGoodsMessage').html("<img src='img/error.svg' style='width: 21px;height: 21px;' alt='错误图片'/><span>&nbsp;发生错误，请重试</span>");
            }
            $('#addGoodsBtn').removeAttr('disabled');
        },
        error: function () {
            $('#addGoodsMessage').html("<img src='img/error.svg' style='width: 21px;height: 21px;' alt='错误图片'/><span>&nbsp;发生错误，请重试</span>");
            $('#addGoodsBtn').removeAttr('disabled');
        }
    });
}

//初始化富文本编辑器
var richTextEditor;
var richTextEditorStatus = 0;

function richTextEditorSetup() {
    if (richTextEditorStatus === 0) {
        richTextEditor = new wangEditor('richTextEditor');
        wangEditor.config.printLog = false;
        richTextEditor.config.menus = ['source', 'bold', 'underline', 'italic', 'strikethrough', 'eraser', 'forecolor', 'bgcolor', 'quote', 'fontfamily', 'fontsize', 'head', 'unorderlist', 'orderlist', 'alignleft', 'aligncenter', 'alignright', 'link', 'unlink', 'table', 'img', 'insertcode', 'undo', 'redo'];
        richTextEditor.create();
        richTextEditor.$txt.html('<p>请在此编辑商品图文介绍...</p>');
        richTextEditorStatus = 1;
    }
}

//顾客查看商品
function buyerViewGoods(goodsId) {
    $('#viewGoodsName').html('加载中...');
    $('#viewGoodsPrice').html('加载中...');
    $('#viewGoodsSellers').html('加载中...');
    $('#viewGoodsSort').html('加载中...');
    $('#viewGoodsIntroduction').html("<div class='text-center'><img src='img/loading.svg' class='loading' style='width: 42px;height: 42px;' alt='加载图片'/><br /><span style='font-size: 2em;'>请稍后</span></div>");
    $('#viewGoodsModal').modal('show');
    $.ajax({
        url: SA + 'buyerController/showGoods.do',
        data: {
            goodsId: goodsId
        },
        dataType: 'json',
        success: function (data) {
            if (!jQuery.isEmptyObject(data)) {
                var viewGoodsSub = $('#viewGoodsSub');
                if (data['subscription'] === 'true') {
                    viewGoodsSub.attr('src', 'img/star-full.svg');
                } else {
                    viewGoodsSub.attr('src', 'img/star.svg');
                }
                viewGoodsSub.attr('data-goodsId', data['goods']['id']);
                $('#viewGoodsName').html(data['goods']['name']);
                $('#viewGoodsPrice').html('&yen;' + data['goods']['price']);
                $('#viewGoodsSellers').html("<a href='javascript:void(0);' onclick='sellersEnterShop(" + data['goods']['userId'] + ");' data-dismiss='modal'>" + data['sellersName'] + "</a>");
                $('#viewGoodsSort').html(data['goods']['sort']);
                $('#viewGoodsIntroduction').html(data['goods']['introduction']);
                $('img', '#viewGoodsIntroduction').addClass('img-responsive');
                $('table', '#viewGoodsIntroduction').addClass('table-bordered');
            } else {
                $('#viewGoodsName').html('加载失败');
                $('#viewGoodsPrice').html('加载失败');
                $('#viewGoodsSellers').html('加载失败');
                $('#viewGoodsSort').html('加载失败');
                $('#viewGoodsIntroduction').html("<div class='text-center'><img src='img/error.svg' style='width: 42px;height: 42px;' alt='错误图片'/><br /><span style='font-size: 2em;'>发生错误，请重试</span></div>");
            }
        },
        error: function () {
            $('#viewGoodsName').html('加载失败');
            $('#viewGoodsPrice').html('加载失败');
            $('#viewGoodsSellers').html('加载失败');
            $('#viewGoodsSort').html('加载失败');
            $('#viewGoodsIntroduction').html("<div class='text-center'><img src='img/error.svg' style='width: 42px;height: 42px;' alt='错误图片'/><br /><span style='font-size: 2em;'>发生错误，请重试</span></div>");
        }
    });
}

//通过关键词查询商品
function selectLikeGoods(keyword, selectAll) {
    selectAll = (selectAll === 'true') ? 'true' : 'false';
    var provinceBtn = $('#provinceBtn').html().trim();
    var province = provinceBtn.substring(0, provinceBtn.indexOf('&')).trim();
    $.ajax({
        url: SA + 'buyerController/selectLikeGoods.do',
        data: {
            keyword: keyword,
            sellerId: getQueryString('sellerId'),
            province: province,
            lowestPrice: $('#lowestPrice').val(),
            highestPrice: $('#highestPrice').val(),
            selectAll: selectAll
        },
        dataType: 'json',
        success: function (data) {
            var content;
            if (!jQuery.isEmptyObject(data['goods'])) {
                content = '小明找到下面这些商品，有你喜欢的吗？';
                for (var i = 0; i < data['goods'].length; i++) {
                    content += "<br/>\"" + data['goods'][i]['name'] + "\"，<a href='javascript:void(0);' onclick='buyerViewGoods(" + data['goods'][i]['id'] + ")'>立即查看</a>";
                }
                if (data['selectAll'] === 'false') {
                    content += "<br/><a href='javascript:void(0);' onclick='selectLikeGoods(\"" + keyword + "\",\"true\")'>显示更多</a>";
                }
                if (data['subscription'] === 'true') {
                    content += "&nbsp;<img class='starIcon' src='img/star-full.svg' data-keyword='" + keyword + "' onclick='subscription(this)' alt='收藏图标'/>";
                } else {
                    content += "&nbsp;<img class='starIcon' src='img/star.svg' data-keyword='" + keyword + "' onclick='subscription(this)' alt='未收藏图标'/>";
                }
            } else {
                content = '抱歉，小明没有找到关于\"' + keyword + '\"的商品。';
                content += "<br/><a href='https://s.taobao.com/search?q=" + keyword + "' target='_blank'>去淘宝看看</a>";
            }
            acceptMessage('robot', content);
        },
        error: function () {
            acceptMessage('robot', '小明出了点问题，请你重试一下呗。');
        }
    });
}

//收藏动作
function subscription(subDom) {
    if (userStatus === 'offline') {
        $('.modal').modal('hide');
        $('#userLoginAndRegModal').modal('show');
        return;
    }
    var subIcon = $(subDom);
    var opera;
    if (subIcon.attr('src') === 'img/star.svg') {
        opera = 'add';
    } else {
        opera = 'remove';
    }
    var type;
    var content;
    if (subIcon.attr('data-keyword') !== undefined) {
        content = subIcon.attr('data-keyword');
        type = 'select';
    } else {
        content = subIcon.attr('data-goodsId');
        type = 'goods';
    }
    $.ajax({
        url: SA + 'featuresController/subscription.do',
        data: {
            opera: opera,
            type: type,
            content: content
        },
        dataType: 'text',
        success: function (data) {
            if (data === 'successAdd' || data === 'successRemove' || data === 'exist') {
                if (subIcon.attr('src') === 'img/star.svg') {
                    subIcon.attr('src', 'img/star-full.svg');
                } else {
                    subIcon.attr('src', 'img/star.svg');
                }
            } else {
                subIcon.attr('src', 'img/error.svg');
            }
        },
        error: function () {
            subIcon.attr('src', 'img/error.svg');
        }
    });
}

//页面加载完成事件
$(document).ready(function () {
    response();
    var email = localStorage.getItem('email');
    var password = localStorage.getItem('password');
    if (email !== null && password !== null) {
        $('#loginUserEmail').val(email);
        $('#loginUserPassword').val(password);
        userLogin();
    } else {
        initPage();
    }
});
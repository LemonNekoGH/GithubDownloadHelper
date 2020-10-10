package moe.nekonest.gdh

import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody

@Controller
class ErrorHandler : ErrorController {
    override fun getErrorPath() = "/error"

    @ResponseBody
    @RequestMapping("/error")
    fun error() = """
<!DOCTYPE html>

<head>
    <style>
        @import url("https://fonts.googleapis.com/css?family=Quicksand:400,500,700&subset=latin-ext");
        body {
            display: flex;
            align-items: center;
            justify-content: center;
            font-family: 'din_alternate_bold';
            height: 100vh;
            margin: 0;
            line-height: 1.8;
            background-color: #F9FAFB;
        }

        .img {
            box-shadow: 3px;
        }

        a {
            text-decoration: none;
            padding: 10px 22px;
            font-size: 12px;
            color: #ffffff;
            line-height: 16px;
            background: #324e63;
            border-radius: 22px;
            cursor: pointer;
            float: right;
            margin-top: 130px;
        }

        .p {
            font-family: 'Courier New', Courier, monospace;
            font-size: 45px;
            color: #324e63;
            margin-bottom: -10px;
        }

        .span{
            font-family: 'Quicksand', sans-serif;
            font-size: 18px;
            color: #324e63;
            margin-top: 20px;
            margin-bottom: 20px;
            margin-left: 10px;
        }

        .box {
            background: #ffffff;
            border-radius: 20px;
            max-width: 500px;
            max-height: 600px;
            width: 50%;
            height: 50%;
            padding: 40px 40px;
            box-shadow: 0px 12px 20px 0px rgba(158, 158, 158, 0.1);
        }
    </style>
</head>

<body>
    <div class="box">
        <div class="p">404 NotFound</div>
        <div class="span"> 文件被猫猫吃掉了 Σ( ° △ °|||)</div>
        <a href="javascript:window.opener=null;window.open('','_self');window.close();">关闭页面</a>
    </div>
</body>
    """.trimIndent()
}
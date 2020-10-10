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
        <h1>文件被猫猫吃掉了 Σ（ﾟдﾟlll）</h1><hr/>
        <span>错误代码</span><br/>
        error code 404: File not found.
    """.trimIndent()
}
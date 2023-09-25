import cn.hutool.core.io.resource.ResourceUtil
import org.junit.Test

class ChineseTest {

    @Test
    fun test() {
        println(ResourceUtil.readUtf8Str("language/zh_cn.yml"))
    }
}
package   com.ruimeng.things.msg.bean

data class MsgListBean(
    var `data`: List<Data> = listOf(),
    var errcode: Int = 0, // 200
    var errmsg: String = "" // 操作成功
) {
    data class Data(
        var content: String = "", // 测试标题2
        var created: String = "0", // 0
        var id: String = "", // 2
        var img: String = "", // http://cdn.tk.image.xianlubang.com/201804281827399607.png
        var title: String = "", // 测试标题2
        var url: String = "", // http://xianglilai.app.xianlubang.com//appweb/appmsg?id=2
        var is_read: Int = 0// int 是否阅读 1是0否
    )
}
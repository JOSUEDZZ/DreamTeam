package mx.tecnm.lapaz.ubertec

data class Platillo(
    var id:Int,
    var nombre:String,
    var descripcion:String,
    var precio:Double,
    var categoria_id:Int = 0
)
package mx.tecnm.lapaz.ubertec

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class PlatillosAdapter(var listaPlatillos:List<Platillo>,
                       val onClick:(Platillo)-> Unit,
                       val activity: Activity) :
    RecyclerView.Adapter<PlatillosAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivFoto:ImageView
        val tvNombre: TextView
        val tvDescripcion:TextView
        val tvPrecio: TextView
        init {
            ivFoto = view.findViewById(R.id.ivFoto)
            tvNombre = view.findViewById(R.id.tvNombre)
            tvDescripcion = view.findViewById(R.id.tvDescripcion)
            tvPrecio = view.findViewById(R.id.tvPrecio)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.renglon_platillo, viewGroup, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        CoroutineScope(Job()).launch {
            val bitmap = WebServiceREST.getImage(
                "http://192.168.0.23/restaurant/public/storage/${listaPlatillos[position].id}.jpg")
            activity.runOnUiThread {
                viewHolder.ivFoto.setImageBitmap(bitmap)
            }
        }
        viewHolder.ivFoto.setOnClickListener{onClick(listaPlatillos[position])}
        viewHolder.tvNombre.text = listaPlatillos[position].nombre
        viewHolder.tvDescripcion.text = listaPlatillos[position].descripcion
        viewHolder.tvPrecio.text = listaPlatillos[position].precio.toString()

        //viewHolder.itemView.setOnClickListener {
        //    clickListener(listaPlatillos[position])
        //}

    }

    override fun getItemCount() = listaPlatillos.size
}

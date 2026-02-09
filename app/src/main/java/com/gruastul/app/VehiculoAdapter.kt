package com.gruastul.app

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.gruastul.app.databinding.ItemVehiculoBinding
import com.gruastul.app.models.VehiculoTipo

class VehiculoAdapter(
    private val vehiculos: List<VehiculoTipo>,
    private val onClick: (VehiculoTipo) -> Unit
) : RecyclerView.Adapter<VehiculoAdapter.VehiculoViewHolder>() {

    private var selectedPosition = -1

    inner class VehiculoViewHolder(val binding: ItemVehiculoBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VehiculoViewHolder {
        val binding = ItemVehiculoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VehiculoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VehiculoViewHolder, position: Int) {
        val vehiculo = vehiculos[position]

        holder.binding.apply {
            tvNombreVehiculo.text = vehiculo.nombre
            tvPrecioBase.text = "Base: $${vehiculo.precioBase.toInt()}"
            tvPrecioPorKm.text = "$${vehiculo.precioPorKm.toInt()}/km"
            tvCategoria.text = "Cat. ${vehiculo.categoria}"

            // Cambiar el color según si está seleccionado
            val isSelected = position == selectedPosition

            if (isSelected) {
                root.strokeWidth = 8
                root.setCardBackgroundColor(
                    ContextCompat.getColor(root.context, android.R.color.holo_blue_light)
                )
                root.strokeColor = ContextCompat.getColor(root.context, android.R.color.holo_blue_dark)
            } else {
                root.strokeWidth = 2
                root.setCardBackgroundColor(
                    ContextCompat.getColor(root.context, android.R.color.white)
                )
                root.strokeColor = ContextCompat.getColor(root.context, android.R.color.darker_gray)
            }

            // Click en la tarjeta
            root.setOnClickListener {
                val oldPosition = selectedPosition
                selectedPosition = holder.bindingAdapterPosition

                // Notificar los cambios
                if (oldPosition != -1) {
                    notifyItemChanged(oldPosition)
                }
                notifyItemChanged(selectedPosition)

                // Llamar al callback
                onClick(vehiculo)
            }
        }
    }

    override fun getItemCount() = vehiculos.size
}
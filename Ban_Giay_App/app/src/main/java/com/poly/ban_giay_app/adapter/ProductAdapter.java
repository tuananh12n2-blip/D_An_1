package com.poly.ban_giay_app.adapter;

import android.content.Intent;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StrikethroughSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.poly.ban_giay_app.ProductDetailActivity;
import com.poly.ban_giay_app.R;
import com.poly.ban_giay_app.models.Product;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.VH> {
    private List<Product> items;

    public ProductAdapter(List<Product> items) {
        this.items = items;
    }

    public static class VH extends RecyclerView.ViewHolder {
        ImageView img;
        TextView name, priceOld, priceNew;

        public VH(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.imgProduct);
            name = itemView.findViewById(R.id.txtName);
            priceOld = itemView.findViewById(R.id.txtPriceOld);
            priceNew = itemView.findViewById(R.id.txtPriceNew);
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Product p = items.get(position);
        holder.img.setImageResource(p.imageRes);
        holder.name.setText(p.name);

        if (p.priceOld != null && !p.priceOld.isEmpty()) {
            SpannableString ss = new SpannableString(p.priceOld);
            ss.setSpan(new StrikethroughSpan(), 0, p.priceOld.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.priceOld.setVisibility(View.VISIBLE);
            holder.priceOld.setText(ss);
        } else {
            holder.priceOld.setVisibility(View.GONE);
        }

        if (p.priceNew != null && !p.priceNew.isEmpty()) {
            holder.priceNew.setVisibility(View.VISIBLE);
            holder.priceNew.setText("Giá: " + p.priceNew);
        } else {
            holder.priceNew.setVisibility(View.GONE);
        }
        
        // Click listener for product image
        holder.img.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ProductDetailActivity.class);
            intent.putExtra("product", p);
            v.getContext().startActivity(intent);
        });
        
        // Click listener for entire item
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ProductDetailActivity.class);
            intent.putExtra("product", p);
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}

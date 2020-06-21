package com.abhi.voicesearch.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FixedApp(var pname:String,var order:Int,var name:String, var inserted:Boolean ) : Parcelable
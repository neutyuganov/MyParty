package com.example.myparty

import android.content.res.Resources
import com.faltenreich.skeletonlayout.Skeleton

class SkeletonClass {
    fun skeletonShow(skeleton: Skeleton, resources: Resources) {
        skeleton.maskCornerRadius = 25f
        skeleton.maskColor = resources.getColor(R.color.stroke_color)
        skeleton.shimmerColor = resources.getColor(R.color.shimmer_color)
        skeleton.shimmerAngle = 45
        skeleton.showSkeleton()
    }
}
package kr.ac.konkuk.githubrepository.extensions

import android.content.res.Resources

//확장 함수
internal fun Float.fromDpToPx(): Int {
    return (this * Resources.getSystem().displayMetrics.density).toInt()
}
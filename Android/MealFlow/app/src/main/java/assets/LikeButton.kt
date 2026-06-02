//package assets
//
//import androidx.compose.animation.animateColorAsState
//import androidx.compose.animation.core.animateDpAsState
//import androidx.compose.foundation.layout.size
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.outlined.ThumbUp
//import androidx.compose.material3.Icon
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.getValue
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.graphicsLayer
//import androidx.compose.ui.unit.dp
//import com.airbnb.lottie.compose.*
//
//
//@Composable
//fun LikeButton(
//    isLiked: Boolean,
//    onToggle: () -> Unit
//) {
//    val composition by rememberLottieComposition(
//        LottieCompositionSpec.Asset("like_animation.json") // تأكد من أن الملف موجود في مجلد assets
//    )
//
//    val animatable = rememberLottieAnimatable()
//
//    // تغير اللون والحجم بناءً على حالة الإعجاب
//    val iconColor by animateColorAsState(
//        targetValue = if (isLiked) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
//    )
//
//    val iconSize by animateDpAsState(
//        targetValue = if (isLiked) 50.dp else 40.dp // تكبير الأيقونة عند الإعجاب
//    )
//
//    // تأكد من أن الأنيميشن يعمل بناءً على حالة الإعجاب
//    LaunchedEffect(isLiked) {
//        if (composition != null) {
//            if (isLiked) {
//                animatable.animate(
//                    composition,
//                    iterations = 1,
//                    speed = 1f
//                )
//            } else {
//                animatable.animate(
//                    composition,
//                    iterations = 1,
//                    speed = -1f
//                )
//            }
//        }
//    }
//
//    // زر الضغط على الأنيميشن مع التفاعل عند الضغط
//    IconButton(onClick = onToggle) {
//        LottieAnimation(
//            composition = composition,
//            progress = { animatable.progress },
//            modifier = Modifier
//                .size(iconSize) // تغيير حجم الأيقونة بناءً على حالة الإعجاب
//                .graphicsLayer(
//                    alpha = 1f // يمكنك إضافة تأثيرات إضافية مثل الشفافية هنا
//                )
//        )
//    }
//}
//
//
////@Composable
////fun LikeButton(
////    isLiked: Boolean,
////    onToggle: () -> Unit
////) {
////    val composition by rememberLottieComposition(
////        LottieCompositionSpec.Asset("like_animation.json") // اسم الملف اللي حطيتُه في assets
////    )
////
////    val progress by animateLottieCompositionAsState(
////        composition = composition,
////        isPlaying = isLiked,
////        speed = if (isLiked) 1f else -1f, // يشغل عكسياً عند الإلغاء
////        iterations = 1,
////        restartOnPlay = true
////    )
////
////    IconButton(onClick = onToggle) {
////        LottieAnimation(
////            composition = composition,
////            progress = { progress },
////            modifier = Modifier.size(48.dp)
////        )
////    }
////}

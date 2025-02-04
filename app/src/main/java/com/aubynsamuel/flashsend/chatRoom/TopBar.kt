package com.aubynsamuel.flashsend.chatRoom

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aubynsamuel.flashsend.R

@Composable
fun HeaderBar(name: String, pic: Int) {
    var expanded by remember { mutableStateOf(false) }
    Column {

        Row(
            modifier = Modifier
                .height(80.dp)
                .fillMaxWidth(1f)
                .background(Color.DarkGray)
                .padding(top = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                Icons.AutoMirrored.Default.ArrowBack,
                contentDescription = "back button",
                modifier = Modifier
                    .padding(start = 15.dp)
                    .size(24.dp), tint = Color.White
            )
            Text(
                text = name,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 25.dp),
                color = Color.White
            )
            Image(
                painter = painterResource(id = pic),
                contentDescription = "",
                modifier = Modifier.clickable(onClick = { expanded = !expanded })
            )
        }
        AnimatedVisibility(expanded, modifier = Modifier.align(Alignment.End)) {
            Image(
                painter = painterResource(id = pic),
                contentDescription = "",
                modifier = Modifier
                    .clickable(onClick = { expanded = !expanded })
                    .size(500.dp)
                    .background(Color.DarkGray),
                alignment = Alignment.Center
            )
        }
    }
}

@Preview
@Composable
fun PrevHeaderBar() {
    HeaderBar(name = "Samuel", pic = R.drawable.ic_launcher_foreground)
}

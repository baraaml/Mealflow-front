package com.example.mealflow.ui.screens

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.mealflow.R
import com.example.mealflow.network.CommunityMember
import com.example.mealflow.network.CommunityMembersApiService
import com.example.mealflow.network.SetAdminsClass
import com.example.mealflow.network.UserMember
import kotlinx.coroutines.launch

//@Composable
//fun CommunityMemberItem(
//    username: String,
//    isAdmin: Boolean,
//    onMakeAdminClick: (onComplete: () -> Unit) -> Unit
//) {
//    var isPromoting by remember { mutableStateOf(false) }
//
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 8.dp, horizontal = 16.dp),
//        verticalAlignment = Alignment.CenterVertically,
//        horizontalArrangement = Arrangement.SpaceBetween
//    ) {
//        Row(verticalAlignment = Alignment.CenterVertically) {
//            Box(
//                modifier = Modifier
//                    .size(42.dp)
//                    .clip(CircleShape)
//                    .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f))
//                    .border(
//                        width = 2.dp,
//                        brush = Brush.linearGradient(
//                            colors = listOf(
//                                MaterialTheme.colorScheme.secondary,
//                                MaterialTheme.colorScheme.tertiary
//                            )
//                        ),
//                        shape = CircleShape
//                    )
//            ) {
//                Image(
//                    painter = painterResource(R.drawable.apple_logo_icon),
//                    contentDescription = "Profile Picture",
//                    modifier = Modifier
//                        .size(28.dp)
//                        .align(Alignment.Center),
//                    contentScale = ContentScale.Fit
//                )
//            }
//
//            Spacer(modifier = Modifier.width(12.dp))
//
//            Text(
//                text = username,
//                style = MaterialTheme.typography.bodyLarge
//            )
//        }
//
//        if (!isAdmin) {
//            Button(
//                onClick = {
//                    isPromoting = true
//                    onMakeAdminClick {
//                        isPromoting = false
//                    }
//                },
//                shape = RoundedCornerShape(12.dp),
//                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0077FF))
//            ) {
//                if (isPromoting) {
//                    CircularProgressIndicator(
//                        color = Color.White,
//                        strokeWidth = 2.dp,
//                        modifier = Modifier.size(18.dp)
//                    )
//                } else {
//                    Text("Make Admin")
//                }
//            }
//        } else {
//            Text(
//                "Admin",
//                color = MaterialTheme.colorScheme.primary,
//                modifier = Modifier
//                    .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
//                    .padding(horizontal = 8.dp, vertical = 4.dp)
//            )
//        }
//    }
//}
//
//@Composable
//fun SetAdminPage(
//    context: Context = LocalContext.current,
//    navController: NavController
//) {
//    val coroutineScope = rememberCoroutineScope()
//    var membersState by remember { mutableStateOf<List<CommunityMember>>(emptyList()) }
//
//    LaunchedEffect(Unit) {
//        coroutineScope.launch {
//            val communityMembers = CommunityMembersApiService(context).fetchCommunityMembers()
//            if (communityMembers.success) {
//                membersState = communityMembers.data.members
//            }
//        }
//    }
//
//    Column {
//        AppBarMembers(
//            title = "Set Admin",
//            onBackClick = { navController.popBackStack() }
//        )
//        membersState.forEachIndexed { index, member ->
//            CommunityMemberItem(
//                username = member.user.username,
//                isAdmin = member.role == "ADMIN",
//                onMakeAdminClick = {
//                    coroutineScope.launch {
//                        // Simulation of the upgrade process
//                        kotlinx.coroutines.delay(2000)
//
//                        val result = SetAdminsClass(context).setAdmins(member.user.id)
//
//                        // Cover both Success and Error cases using when
//                        when (result) {
//                            else -> {
//                                // Update members to show the change
//                                membersState = membersState.map {
//                                    if (it.user.id == member.user.id) {
//                                        it.copy(role = "ADMIN")
//                                    } else {
//                                        it
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            )
//            if (index != membersState.lastIndex) {
//                HorizontalDivider(
//                    modifier = Modifier.padding(vertical = 8.dp)
//                )
//            }
//        }
//    }
//}
@Composable
fun CommunityMemberItem(
    username: String,
    isAdmin: Boolean,
    onMakeAdminClick: (onComplete: () -> Unit) -> Unit
) {
    var isPromoting by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f))
                    .border(
                        width = 2.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.secondary,
                                MaterialTheme.colorScheme.tertiary
                            )
                        ),
                        shape = CircleShape
                    )
            ) {
                Image(
                    painter = painterResource(R.drawable.apple_logo_icon),
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(28.dp)
                        .align(Alignment.Center),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = username,
                style = MaterialTheme.typography.bodyLarge
            )
        }

        if (!isAdmin) {
            Button(
                onClick = {
                    isPromoting = true
                    onMakeAdminClick {
                        isPromoting = false
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0077FF))
            ) {
                if (isPromoting) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Text("Make Admin")
                }
            }
        } else {
            Text(
                "Admin",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun OwnerMemberItemForAdmin(
    owner: UserMember,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .background(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        )
                ) {
                    AsyncImage(
                        model = owner.profilePicture,
                        contentDescription = "Owner Profile Picture",
                        modifier = Modifier
                            .size(28.dp)
                            .align(Alignment.Center)
                            .clip(CircleShape),
                        placeholder = painterResource(R.drawable.loading_placeholder),
                        error = painterResource(R.drawable.profile_default),
                        contentScale = ContentScale.Crop
                    )
                }

                // Crown icon
                Icon(
                    imageVector = Icons.Default.Stars,
                    contentDescription = "Owner Crown",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.TopEnd)
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = CircleShape
                        )
                        .padding(2.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = owner.username,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                val fullName = "${owner.name ?: ""} ${owner.lastName ?: ""}".trim()
                if (fullName.isNotEmpty()) {
                    Text(
                        text = fullName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Owner",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Owner",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // "Already Owner" text
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "Already Owner",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun SetAdminPage(
    context: Context = LocalContext.current,
    navController: NavController
) {
    val coroutineScope = rememberCoroutineScope()
    var membersState by remember { mutableStateOf<List<CommunityMember>>(emptyList()) }
    var ownerState by remember { mutableStateOf<UserMember?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            val communityMembers = CommunityMembersApiService(context).fetchCommunityMembers()
            if (communityMembers.success) {
                membersState = communityMembers.data.members
                ownerState = communityMembers.data.owner
            }
            isLoading = false
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        AppBarMembers(
            title = "Set Admin",
            onBackClick = { navController.popBackStack() }
        )

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                // Display Owner first
                ownerState?.let { owner ->
                    item(key = "owner_${owner.id}") {
                        OwnerMemberItemForAdmin(owner = owner)

                        if (membersState.isNotEmpty()) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
                            )
                        }
                    }
                }

                // Display other members
                if (membersState.isEmpty() && ownerState == null) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No members found",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    itemsIndexed(
                        items = membersState,
                        key = { _, member -> member.user.id }
                    ) { index, member ->
                        CommunityMemberItem(
                            username = member.user.username,
                            isAdmin = member.role == "ADMIN",
                            onMakeAdminClick = { onComplete ->
                                coroutineScope.launch {
                                    // Simulation of the upgrade process
                                    kotlinx.coroutines.delay(2000)

                                    val result = SetAdminsClass(context).setAdmins(member.user.id)

                                    // Cover both Success and Error cases using when
                                    when (result) {
                                        else -> {
                                            // Update members to show the change
                                            membersState = membersState.map {
                                                if (it.user.id == member.user.id) {
                                                    it.copy(role = "ADMIN")
                                                } else {
                                                    it
                                                }
                                            }
                                        }
                                    }
                                    onComplete()
                                }
                            }
                        )

                        if (index != membersState.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
@Preview
@Composable
fun E() {
    SetAdminPage(navController = rememberNavController())
}


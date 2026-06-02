package com.example.mealflow.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.mealflow.R
import com.example.mealflow.database.UserPreferencesManager
import com.example.mealflow.network.joinCommunityApi
import com.example.mealflow.ui.screens.CommunityData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun CardCommunity(
    communityName: String,
    imageUrl: String,
    members: Int,
    recipes: Int,
    onJoinClick: () -> Unit,
    onClick: () -> Unit,
    isMember: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(220.dp)
            .wrapContentHeight(),
        shape = RoundedCornerShape(16.dp),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            // Community image with join button
            Box(
                modifier = Modifier
                    .height(160.dp)
                    .fillMaxWidth()
            ) {
                // Community image
                Image(
                    painter = rememberAsyncImagePainter(
                        model = imageUrl,
                        error = painterResource(R.drawable.neptune_placeholder_48), // Default image on error
                        placeholder = painterResource(R.drawable.loading_placeholder) // Image while loading
                    ),
                    contentDescription = "Community image :$communityName",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Transparent layer for contrast
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.3f)
                                )
                            )
                        )
                )
                if (!isMember) {
                    // Join button with improved UX
                    Button(
                        onClick = onJoinClick,
                        modifier = Modifier
                            .padding(16.dp)
                            .align(Alignment.TopEnd)
                            .heightIn(min = 40.dp)
                            .widthIn(min = 80.dp)
                            .shadow(
                                elevation = 4.dp,
                                shape = RoundedCornerShape(16.dp),
                                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            ),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 2.dp,
                            pressedElevation = 6.dp,
                            hoveredElevation = 4.dp
                        ),
                        contentPadding = PaddingValues(
                            horizontal = 20.dp,
                            vertical = 12.dp
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Join",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 0.5.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
//                if (!isMember) {
//                    // Join button
//                    Button(
//                        onClick = onJoinClick,
//                        modifier = Modifier
//                            .padding(12.dp)
//                            .align(Alignment.TopEnd),
//                        shape = RoundedCornerShape(12.dp),
//                        colors = ButtonDefaults.buttonColors(
//                            containerColor = MaterialTheme.colorScheme.primary
//                        )
//                    ) {
//                        Text(
//                            text = "Join",
//                            style = MaterialTheme.typography.labelMedium.copy(
//                                fontWeight = FontWeight.Bold
//                            )
//                        )
//                    }
//                }
            }


            // Community Information
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = communityName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Icon and number of members
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$members member",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // Icon and number of recipes
                    Icon(
                        imageVector = Icons.Rounded.Restaurant,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "recipe: $recipes",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }

}
@Composable
fun CardCommunityProfile(
    communityName: String,
    imageUrl: String,
    members: Int,
    recipes: Int,
    onJoinClick: () -> Unit,
    onClick: () -> Unit,
    isMember: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .wrapContentHeight()
            .fillMaxWidth()
            .padding(5.dp),
        shape = RoundedCornerShape(16.dp),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            // Community image with join button
            Box(
                modifier = Modifier
                    .height(160.dp)
                    .fillMaxWidth()
            ) {
                // Community image
                Image(
                    painter = rememberAsyncImagePainter(
                        model = imageUrl,
                        error = painterResource(R.drawable.neptune_placeholder_48), // Default image on error
                        placeholder = painterResource(R.drawable.loading_placeholder) // Image while loading
                    ),
                    contentDescription = "Community image :$communityName",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Transparent layer for contrast
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.3f)
                                )
                            )
                        )
                )
            }

            // Community Information
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = communityName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Icon and number of members
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$members member",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // Icon and number of recipes
                    Icon(
                        imageVector = Icons.Rounded.Restaurant,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "recipe: $recipes",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
@Composable
fun CommunityList(
    communities: List<CommunityData>,
    navController: NavController,
    isLoading: Boolean = false // Add loading state parameter
) {
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "My Communities",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "See all",
                fontSize = 14.sp,
                modifier = Modifier.clickable {
                    navController.navigate("MyCommunities Page")
                }
            )
        }
        Spacer(modifier = Modifier.height(8.dp)) // Adds spacing before LazyRow

        // Show loading indicator or communities list
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp), // Give enough height for the loader
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(40.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        } else {
            val userPreferencesManager = remember { UserPreferencesManager(context) }

            if (communities.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No communities found",
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                }
            } else {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(communities, key = { it.id }) { community ->
                        CardCommunity(
                            communityName = community.name,
                            imageUrl = community.imageUrl,
                            members = community.memberCount,
                            recipes = 27,
                            onJoinClick = {
                                joinCommunityApi(community.id, context)
                            },
                            onClick = {
                                CoroutineScope(Dispatchers.IO).launch {
                                    userPreferencesManager.saveCommunityId(community.id)
                                }
                                navController.navigate("Community Page")
                            },
                            isMember = community.isMember
                        )
                    }
                }
            }
        }
    }
}
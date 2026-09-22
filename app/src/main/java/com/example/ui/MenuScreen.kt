package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.MenuItem
import com.example.ui.theme.AmberGold
import com.example.ui.theme.DeepBlack
import com.example.ui.theme.SlateDark

import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    viewModel: MenuViewModel,
    onNavigateToCart: () -> Unit,
    onLogout: () -> Unit
) {
    val uiState by viewModel.betterUiState.collectAsState()
    val categories = listOf("Tea Menu", "Coffee", "Bun Makkhan", "Sandwich", "Maggie")
    val uriHandler = LocalUriHandler.current

    var showFeedbackDialog by remember { mutableStateOf(false) }
    var globalRating by remember { mutableIntStateOf(0) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Lucknawi Tandoori Chai",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = AmberGold
                        )
                    )
                },
                actions = {
                    IconButton(onClick = {
                        showFeedbackDialog = true
                    }) {
                        Icon(Icons.Default.RateReview, contentDescription = "Feedback", tint = AmberGold)
                    }
                    IconButton(onClick = {
                        FirebaseAuth.getInstance().signOut()
                        onLogout()
                    }) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout", tint = AmberGold)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = DeepBlack
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { uriHandler.openUri("tel:9580344482") },
                containerColor = AmberGold,
                contentColor = DeepBlack
            ) {
                Icon(Icons.Default.Phone, contentDescription = "Call Cafe")
            }
        },
        bottomBar = {
            if (uiState.cart.isNotEmpty()) {
                CartBottomBar(
                    cart = uiState.cart,
                    onViewCart = onNavigateToCart
                )
            }
        },
        containerColor = DeepBlack
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Hero Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_hero_banner),
                    contentDescription = "Lucknowi Cafe Banner",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, DeepBlack.copy(alpha = 0.8f))
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Text(
                        "Lucknow ki asli chai ka taste",
                        color = AmberGold,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "सब कबूल है लेकिन तंदूरी चाये के साथ...",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Category Tabs
            ScrollableTabRow(
                selectedTabIndex = categories.indexOf(uiState.selectedCategory),
                containerColor = DeepBlack,
                contentColor = AmberGold,
                edgePadding = 16.dp,
                divider = {}
            ) {
                categories.forEach { category ->
                    Tab(
                        selected = uiState.selectedCategory == category,
                        onClick = { viewModel.selectCategory(category) },
                        text = { Text(category) }
                    )
                }
            }

            // Menu Items
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(uiState.menuItems) { item ->
                    MenuItemCard(
                        item = item,
                        quantity = uiState.cart.find { it.menuItem.id == item.id }?.quantity ?: 0,
                        onAdd = { viewModel.addToCart(item) },
                        onRemove = { viewModel.removeFromCart(item) }
                    )
                }
            }
        }

        // Combo Prompt
        if (uiState.showComboPrompt) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissComboPrompt() },
                confirmButton = {
                    TextButton(onClick = { 
                        viewModel.selectCategory("Bun Makkhan")
                        viewModel.dismissComboPrompt()
                    }) {
                        Text("Add Bun Makkhan", color = AmberGold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismissComboPrompt() }) {
                        Text("Maybe Later", color = Color.Gray)
                    }
                },
                title = { Text("Combo Suggestion", color = AmberGold) },
                text = { Text(uiState.comboMessage, color = Color.White) },
                containerColor = SlateDark
            )
        }

        // Feedback Dialog
        if (showFeedbackDialog) {
            AlertDialog(
                onDismissRequest = { showFeedbackDialog = false },
                title = { Text("Rate Your Experience", color = AmberGold) },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("How do you like our Lucknawi Tandoori Chai?", color = Color.White)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            (1..5).forEach { index ->
                                IconButton(onClick = { globalRating = index }) {
                                    Icon(
                                        imageVector = if (index <= globalRating) Icons.Default.Star else Icons.Default.StarBorder,
                                        contentDescription = "Rate $index stars",
                                        tint = if (index <= globalRating) AmberGold else Color.Gray,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        enabled = globalRating > 0,
                        onClick = {
                            showFeedbackDialog = false
                            android.widget.Toast.makeText(context, "Thank you for the $globalRating star rating!", android.widget.Toast.LENGTH_SHORT).show()
                            globalRating = 0
                        }
                    ) {
                        Text("Submit", color = AmberGold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { 
                        showFeedbackDialog = false 
                        globalRating = 0
                    }) {
                        Text("Cancel", color = Color.Gray)
                    }
                },
                containerColor = SlateDark
            )
        }
    }
}

@Composable
fun MenuItemCard(
    item: MenuItem,
    quantity: Int,
    onAdd: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SlateDark),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = AmberGold,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "₹${item.price.toInt()}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
            
            if (quantity > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier
                            .size(32.dp)
                            .background(AmberGold, CircleShape)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Remove", tint = DeepBlack)
                    }
                    Text(
                        quantity.toString(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    IconButton(
                        onClick = onAdd,
                        modifier = Modifier
                            .size(32.dp)
                            .background(AmberGold, CircleShape)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add", tint = DeepBlack)
                    }
                }
            } else {
                Button(
                    onClick = onAdd,
                    colors = ButtonDefaults.buttonColors(containerColor = AmberGold),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("ADD", color = DeepBlack, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CartBottomBar(
    cart: List<CartItem>,
    onViewCart: () -> Unit
) {
    val totalItems = cart.sumOf { it.quantity }
    val totalPrice = cart.sumOf { it.menuItem.price * it.quantity }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onViewCart() },
        color = AmberGold,
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    "$totalItems Items | ₹${totalPrice.toInt()}",
                    color = DeepBlack,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    "View Cart",
                    color = DeepBlack.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Icon(Icons.Default.ArrowForward, contentDescription = "View Cart", tint = DeepBlack)
        }
    }
}

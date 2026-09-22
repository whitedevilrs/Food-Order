package com.example.ui

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.ui.theme.AmberGold
import com.example.ui.theme.DeepBlack
import com.example.ui.theme.SlateDark
import com.google.android.gms.location.LocationServices
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    viewModel: MenuViewModel,
    onBack: () -> Unit,
    onOrderPlaced: () -> Unit
) {
    val uiState by viewModel.betterUiState.collectAsState()
    var orderType by remember { mutableStateOf("Dine-In") }
    var paymentMethod by remember { mutableStateOf("UPI") }
    
    var customerName by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var locationCoords by remember { mutableStateOf<String?>(null) }
    var isUpiLaunched by remember { mutableStateOf(false) }
    var showQrDialog by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val upiId = "ajaymaurya.7618066709-1@okicici"
    val payeeName = "Ajay Maurya"
    val totalPrice = uiState.cart.sumOf { it.menuItem.price * it.quantity }
    val totalPriceInt = totalPrice.toInt()

    // UPI Payment URLs
    val upiPayUrl = remember(totalPriceInt, customerName) {
        val encodedName = URLEncoder.encode(payeeName, "UTF-8")
        val encodedNote = URLEncoder.encode("Order from ${customerName.ifBlank { "Customer" }}".take(20), "UTF-8")
        "upi://pay?pa=$upiId&pn=$encodedName&am=$totalPriceInt&cu=INR&tn=$encodedNote"
    }

    val upiPayUrlNoAmount = remember(customerName) {
        val encodedName = URLEncoder.encode(payeeName, "UTF-8")
        val encodedNote = URLEncoder.encode("Order from ${customerName.ifBlank { "Customer" }}".take(20), "UTF-8")
        "upi://pay?pa=$upiId&pn=$encodedName&cu=INR&tn=$encodedNote"
    }

    // QR Code URL via QuickChart (more reliable than Google Charts)
    val qrCodeUrl = remember(upiPayUrl) {
        val encodedUpi = URLEncoder.encode(upiPayUrl, "UTF-8")
        "https://quickchart.io/qr?text=$encodedUpi&size=500&margin=1"
    }

    var payWithoutAmount by remember { mutableStateOf(false) }
    val finalUpiUrl = if (payWithoutAmount) upiPayUrlNoAmount else upiPayUrl

    // Reset UPI launch state if payment method changes
    LaunchedEffect(paymentMethod) {
        isUpiLaunched = false
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        locationCoords = "${location.latitude},${location.longitude}"
                    }
                }
            } catch (e: SecurityException) {}
        }
    }

    if (showQrDialog) {
        Dialog(onDismissRequest = { showQrDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SlateDark)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Scan & Pay",
                        style = MaterialTheme.typography.titleLarge,
                        color = AmberGold,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "₹$totalPriceInt",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )
                    if (totalPriceInt > 2000) {
                        Text(
                            "Note: For payments > 2000, you may need to enter the amount manually in your UPI app.",
                            color = Color.Yellow.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Box(
                        modifier = Modifier
                            .size(240.dp)
                            .background(Color.White, RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = qrCodeUrl,
                            contentDescription = "Payment QR Code",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit,
                            onLoading = { /* Show nothing or a placeholder */ },
                            onError = { /* Log error if needed */ }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Payee: $payeeName",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        upiId,
                        color = AmberGold,
                        style = MaterialTheme.typography.bodySmall
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { showQrDialog = false },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = AmberGold)
                    ) {
                        Text("DONE", color = DeepBlack, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Cart", color = AmberGold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = AmberGold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepBlack)
            )
        },
        containerColor = DeepBlack
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            if (uiState.cart.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Your cart is empty", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.cart) { cartItem ->
                        CartItemRow(cartItem)
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Customer Details", color = AmberGold, style = MaterialTheme.typography.titleMedium)
                        
                        OutlinedTextField(
                            value = customerName,
                            onValueChange = { customerName = it },
                            label = { Text("Customer Name") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AmberGold,
                                unfocusedBorderColor = SlateDark,
                                focusedLabelColor = AmberGold,
                                unfocusedLabelColor = Color.Gray,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text("Address / Table Number") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AmberGold,
                                unfocusedBorderColor = SlateDark,
                                focusedLabelColor = AmberGold,
                                unfocusedLabelColor = Color.Gray,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Button(
                            onClick = {
                                val hasFineLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                val hasCoarseLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                
                                if (hasFineLocation || hasCoarseLocation) {
                                    try {
                                        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                            if (location != null) {
                                                locationCoords = "${location.latitude},${location.longitude}"
                                            }
                                        }
                                    } catch (e: SecurityException) {}
                                } else {
                                    locationPermissionLauncher.launch(
                                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SlateDark),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = AmberGold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                if (locationCoords != null) "Location Captured ✅" else "Attach GPS Location",
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Order Type", color = AmberGold, style = MaterialTheme.typography.titleMedium)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OrderTypeChip("Dine-In", orderType == "Dine-In") { orderType = it }
                            OrderTypeChip("Takeaway", orderType == "Takeaway") { orderType = it }
                            OrderTypeChip("Delivery", orderType == "Delivery") { orderType = it }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Payment Method", color = AmberGold, style = MaterialTheme.typography.titleMedium)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OrderTypeChip("UPI", paymentMethod == "UPI") { paymentMethod = it }
                            OrderTypeChip("Cash", paymentMethod == "Cash") { paymentMethod = it }
                        }

                        if (paymentMethod == "UPI") {
                            Spacer(modifier = Modifier.height(12.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SlateDark),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            "Pay to: $payeeName",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(upiId, color = AmberGold, style = MaterialTheme.typography.bodySmall)
                                        IconButton(onClick = {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            val clip = ClipData.newPlainText("UPI ID", upiId)
                                            clipboard.setPrimaryClip(clip)
                                        }) {
                                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = AmberGold, modifier = Modifier.size(20.dp))
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    Button(
                                        onClick = { showQrDialog = true },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = AmberGold.copy(alpha = 0.1f)),
                                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.linearGradient(listOf(AmberGold, AmberGold))),
                                    ) {
                                        Icon(Icons.Default.QrCode, contentDescription = null, tint = AmberGold)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("SHOW SCANNER QR", color = AmberGold)
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "Note: UPI payment is under development, use QR for online payments",
                                        color = Color.Yellow.copy(alpha = 0.7f),
                                        style = MaterialTheme.typography.bodySmall,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    
                                    if (totalPriceInt > 2000) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Checkbox(
                                                checked = payWithoutAmount,
                                                onCheckedChange = { payWithoutAmount = it },
                                                colors = CheckboxDefaults.colors(
                                                    checkedColor = AmberGold,
                                                    uncheckedColor = Color.Gray,
                                                    checkmarkColor = DeepBlack
                                                )
                                            )
                                            Text(
                                                "Fix 2000 limit (Enter amount manually)",
                                                color = Color.Gray,
                                                style = MaterialTheme.typography.bodySmall,
                                                modifier = Modifier.clickable { payWithoutAmount = !payWithoutAmount }
                                            )
                                        }
                                    }
                                }
                            }
                        } else if (paymentMethod == "Cash") {
                            Spacer(modifier = Modifier.height(12.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SlateDark),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = AmberGold,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "Free delivery only 2KM from the located shop",
                                        color = Color.White,
                                        style = MaterialTheme.typography.bodyMedium,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        "Payment will be collected upon delivery/visit.",
                                        color = Color.Gray,
                                        style = MaterialTheme.typography.bodySmall,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Divider(color = SlateDark, modifier = Modifier.padding(vertical = 16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total Amount", color = Color.White, style = MaterialTheme.typography.titleLarge)
                    Text("₹$totalPriceInt", color = AmberGold, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = {
                        if (customerName.isBlank() || address.isBlank() || locationCoords == null) return@Button
                        
                        if (paymentMethod == "UPI" && !isUpiLaunched) {
                            try {
                                uriHandler.openUri(finalUpiUrl)
                                isUpiLaunched = true
                            } catch (e: Exception) {
                                isUpiLaunched = true 
                            }
                        } else {
                            val itemsSummary = uiState.cart.joinToString("\n") { 
                                "- ${it.menuItem.name} x${it.quantity} (₹${(it.menuItem.price * it.quantity).toInt()})" 
                            }
                            
                            val locationString = if (locationCoords != null) {
                                "📍 *Live Location:* https://www.google.com/maps/search/?api=1&query=$locationCoords"
                            } else {
                                "📍 *Location:* Not attached"
                            }

                            val message = "☕ *New Order: Lucknawi Tandoori Chai*\n\n" +
                                    "*Customer Name:* $customerName\n" +
                                    "*Address:* $address\n" +
                                    "$locationString\n\n" +
                                    "*Items:*\n$itemsSummary\n\n" +
                                    "*Total Amount:* ₹$totalPriceInt\n" +
                                    "*Order Type:* $orderType\n" +
                                    "*Payment:* $paymentMethod (ID: $upiId)\n\n" +
                                    "Please confirm the order! 🙏"
                            
                            try {
                                val encodedMessage = URLEncoder.encode(message, "UTF-8")
                                uriHandler.openUri("https://wa.me/918052082094?text=$encodedMessage")
                                onOrderPlaced()
                            } catch (e: Exception) {
                                onOrderPlaced()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = AmberGold),
                    shape = RoundedCornerShape(12.dp),
                    enabled = customerName.isNotBlank() && address.isNotBlank() && locationCoords != null
                ) {
                    val buttonText = when {
                        locationCoords == null -> "ATTACH LOCATION FIRST"
                        paymentMethod == "UPI" && !isUpiLaunched -> "PAY NOW (UPI)"
                        paymentMethod == "UPI" && isUpiLaunched -> "SEND ORDER ON WHATSAPP"
                        else -> "PLACE ORDER"
                    }
                    Text(buttonText, color = DeepBlack, fontWeight = FontWeight.Bold, modifier = Modifier.padding(8.dp))
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedButton(
                    onClick = { uriHandler.openUri("tel:8052082094") },
                    modifier = Modifier.fillMaxWidth(),
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.linearGradient(listOf(AmberGold, AmberGold))),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = AmberGold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("CALL TO TRACK", color = AmberGold)
                }
            }
        }
    }
}

@Composable
fun CartItemRow(cartItem: CartItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SlateDark, RoundedCornerShape(8.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(cartItem.menuItem.name, color = Color.White, fontWeight = FontWeight.Bold)
            Text("${cartItem.quantity} x ₹${cartItem.menuItem.price.toInt()}", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
        }
        Text("₹${(cartItem.menuItem.price * cartItem.quantity).toInt()}", color = AmberGold, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun OrderTypeChip(
    label: String,
    selected: Boolean,
    onSelect: (String) -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = { onSelect(label) },
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = AmberGold,
            selectedLabelColor = DeepBlack,
            labelColor = Color.White,
            containerColor = SlateDark
        )
    )
}

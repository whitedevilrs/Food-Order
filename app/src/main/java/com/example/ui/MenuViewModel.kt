package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.MenuItem
import com.example.data.MenuRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CartItem(
    val menuItem: MenuItem,
    val quantity: Int
)

data class MenuUiState(
    val menuItems: List<MenuItem> = emptyList(),
    val cart: List<CartItem> = emptyList(),
    val selectedCategory: String = "Tea Menu",
    val showComboPrompt: Boolean = false,
    val comboMessage: String = ""
)

class MenuViewModel(private val repository: MenuRepository) : ViewModel() {

    private val _selectedCategory = MutableStateFlow("Tea Menu")
    private val _cart = MutableStateFlow<List<CartItem>>(emptyList())
    private val _showComboPrompt = MutableStateFlow(false)
    private val _comboMessage = MutableStateFlow("")
    
    val uiState: StateFlow<MenuUiState> = combine(
        repository.getMenuItemsByCategory(_selectedCategory.value),
        _cart,
        _selectedCategory,
        _showComboPrompt,
        _comboMessage
    ) { menuItems, cart, category, showCombo, comboMsg ->
        MenuUiState(
            menuItems = menuItems,
            cart = cart,
            selectedCategory = category,
            showComboPrompt = showCombo,
            comboMessage = comboMsg
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MenuUiState()
    )

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }
    
    // Better combine pattern
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val betterUiState: StateFlow<MenuUiState> = _selectedCategory
        .flatMapLatest { category ->
            combine(
                repository.getMenuItemsByCategory(category),
                _cart,
                _showComboPrompt,
                _comboMessage
            ) { menuItems, cart, showCombo, comboMsg ->
                MenuUiState(
                    menuItems = menuItems,
                    cart = cart,
                    selectedCategory = category,
                    showComboPrompt = showCombo,
                    comboMessage = comboMsg
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = MenuUiState()
        )

    fun addToCart(menuItem: MenuItem) {
        val currentCart = _cart.value.toMutableList()
        val existingItem = currentCart.find { it.menuItem.id == menuItem.id }
        
        if (existingItem != null) {
            val index = currentCart.indexOf(existingItem)
            currentCart[index] = existingItem.copy(quantity = existingItem.quantity + 1)
        } else {
            currentCart.add(CartItem(menuItem, 1))
        }
        
        _cart.value = currentCart
        
        // Combo Logic: "Butter Bun ke saath chai ka maza double!"
        if (menuItem.category == "Tea Menu" && !currentCart.any { it.menuItem.category == "Bun Makkhan" }) {
            _comboMessage.value = "Butter Bun ke saath chai ka maza double!"
            _showComboPrompt.value = true
        }
    }

    fun removeFromCart(menuItem: MenuItem) {
        val currentCart = _cart.value.toMutableList()
        val existingItem = currentCart.find { it.menuItem.id == menuItem.id }
        
        if (existingItem != null) {
            if (existingItem.quantity > 1) {
                val index = currentCart.indexOf(existingItem)
                currentCart[index] = existingItem.copy(quantity = existingItem.quantity - 1)
            } else {
                currentCart.remove(existingItem)
            }
        }
        _cart.value = currentCart
    }

    fun dismissComboPrompt() {
        _showComboPrompt.value = false
    }
}

class MenuViewModelFactory(private val repository: MenuRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MenuViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MenuViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

package com.example.data

import kotlinx.coroutines.flow.Flow

class MenuRepository(private val menuDao: MenuDao) {
    val allMenuItems: Flow<List<MenuItem>> = menuDao.getAllMenuItems()

    fun getMenuItemsByCategory(category: String): Flow<List<MenuItem>> {
        return menuDao.getMenuItemsByCategory(category)
    }
}

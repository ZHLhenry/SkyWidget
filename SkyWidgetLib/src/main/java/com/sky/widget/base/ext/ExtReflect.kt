/**
 * 反射访问扩展工具
 *
 * 提供安全访问私有字段、方法的扩展函数，自动处理
 * accessible 状态恢复和 final 字段修改。
 */
package com.sky.widget.base.ext

import android.annotation.SuppressLint
import android.os.Build
import java.lang.reflect.AccessibleObject
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Modifier

/**
 * 修改 Field 的 modifiers，用于解除 final 限制
 *
 * 兼容 Android 5.1 以上和以下两种场景：
 * - API > 21: 直接修改 Field 内部的 accessFlags
 * - API <= 21: 通过 ArtField 间接修改
 */
@SuppressLint("ObsoleteSdkInt")
private fun Field?.editModifiers(modifiers: Int) {
    if (modifiers == -1) {
        return
    }
    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
        val accessFlagsField = Field::class.java.getDeclaredField("accessFlags")
        accessFlagsField.isAccessible = true
        accessFlagsField.setInt(this, modifiers)
    } else {
        val artField = Field::class.java.getDeclaredField("artField")
        artField.isAccessible = true
        val artF = artField.get(this)
        val artFieldClazz = Class.forName("java.lang.reflect.ArtField")
        val accessFlagsField = artFieldClazz.getDeclaredField("accessFlags")
        accessFlagsField.isAccessible = true
        accessFlagsField.setInt(artF, modifiers)
    }
}

/**
 * 安全访问可访问对象，执行完后自动恢复原始状态
 *
 * @param ignoreFinal 是否临时解除 final 限制
 * @param block 访问操作
 */
fun <T : AccessibleObject, R : Any> T.safeAccess(ignoreFinal: Boolean = true, block: (T) -> R?): R? {
    var result: R? = null
    try {
        val originAccessible = isAccessible
        var modifiers: Int = -1
        isAccessible = true
        if (ignoreFinal) {
            if (this is Field) {
                modifiers = this.modifiers
                if (Modifier.isFinal(modifiers)) {
                    this.editModifiers(modifiers.xor(Modifier.FINAL))
                }
            }
        }
        result = block.invoke(this)
        if (ignoreFinal) {
            if (this is Field) {
                this.editModifiers(modifiers)
            }
        }
        isAccessible = originAccessible
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return result
}

/** 递归查找指定名称的字段，包括父类 */
private fun Class<*>.findField(fieldName: String): Field? {
    val field = declaredFields.find { it.name == fieldName }
    if (null == field) {
        if (superclass == Any::class.java) {
            return null
        }
        return superclass.findField(fieldName)
    } else {
        return field
    }
}

/** 获取指定名称的字段，未找到时抛出异常 */
private fun <T : Any> T.getField(fieldName: String): Field {
    return this::class.java.findField(fieldName)
        ?: throw Exception("not found $fieldName in ${this::class.java}")
}

/** 递归查找指定名称的方法，包括父类 */
private fun Class<*>.findFun(funName: String): Method? {
    val method = declaredMethods.find { it.name == funName }
    if (null == method) {
        if (superclass == Any::class.java) {
            return null
        }
        return superclass.findFun(funName)
    } else {
        return method
    }
}

/** 获取指定名称的方法，未找到时抛出异常 */
private fun <T : Any> T.getFun(funName: String): Method {
    return this::class.java.findFun(funName)
        ?: throw Exception("not found $funName in ${this::class.java}")
}

/**
 * 设置字段值
 *
 * @param fieldName 字段名
 * @param value 新值
 * @return 是否设置成功
 */
fun <T : Any, V : Any> T?.setFieldValue(
    fieldName: String,
    value: V?,
): Boolean {
    if (null == this) {
        return false
    }
    getField(fieldName).safeAccess { it.set(this, value) }
    return true
}

/**
 * 获取字段值（强制类型转换）
 *
 * @param fieldName 字段名
 * @return 字段值，类型不匹配时抛出 ClassCastException
 */
@Suppress("UNCHECKED_CAST")
fun <T : Any, V : Any> T.getFieldValue(fieldName: String): V {
    return getField(fieldName).safeAccess { it.get(this) } as V
}

/**
 * 安全获取字段原始值，不做类型强转，避免数组等类型的 ClassCastException
 *
 * @param fieldName 字段名
 * @return 字段原始值，获取失败返回 null
 */
fun <T : Any> T.getRawFieldValue(fieldName: String): Any? {
    return getField(fieldName).safeAccess { it.get(this) }
}

/** 安全获取字段值并尝试类型转换，失败时返回 null */
@Suppress("UNCHECKED_CAST")
fun <T : Any, V : Any> T.safeGetFieldValue(fieldName: String): V? {
    return getField(fieldName).safeAccess { it.get(this) } as? V
}

/** 安全调用指定名称的方法 */
@Suppress("UNCHECKED_CAST")
fun <T : Any, V : Any> T?.invokeMethod(funName: String, vararg args: Any?): V? {
    val self = this
        ?: return null
    return getFun(funName).safeAccess { it.invoke(self, args) } as? V
}

package com.example.ui.components

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.model.FileItem
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileOptionsBottomSheet(
    item: FileItem,
    onDismiss: () -> Unit,
    onOpen: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onCopy: () -> Unit,
    onCut: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDetails: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp, start = 16.dp, end = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val (icon, tint) = FileIconHelper.getIconAndColor(item)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(tint.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${item.formattedSize} • ${item.formattedDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Primary actions
            ActionRowItem(
                icon = Icons.Outlined.OpenInNew,
                title = "فتح",
                onClick = {
                    onDismiss()
                    onOpen()
                }
            )

            ActionRowItem(
                icon = Icons.Outlined.ContentCopy,
                title = "نسخ",
                onClick = {
                    onDismiss()
                    onCopy()
                }
            )

            ActionRowItem(
                icon = Icons.Outlined.ContentCut,
                title = "نقل (قص)",
                onClick = {
                    onDismiss()
                    onCut()
                }
            )

            ActionRowItem(
                icon = if (item.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                title = if (item.isFavorite) "إزالة من المفضلة" else "إضافة للمفضلة",
                onClick = {
                    onDismiss()
                    onToggleFavorite()
                }
            )

            ActionRowItem(
                icon = Icons.Outlined.Edit,
                title = "إعادة تسمية",
                onClick = {
                    onDismiss()
                    onRename()
                }
            )

            ActionRowItem(
                icon = Icons.Outlined.Share,
                title = "مشاركة الملف",
                onClick = {
                    onDismiss()
                    try {
                        val file = File(item.path)
                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "*/*"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(intent, "مشاركة الملف"))
                    } catch (_: Exception) {}
                }
            )

            ActionRowItem(
                icon = Icons.Outlined.Info,
                title = "التفاصيل",
                onClick = {
                    onDismiss()
                    onDetails()
                }
            )

            ActionRowItem(
                icon = Icons.Outlined.Delete,
                title = "حذف",
                tint = MaterialTheme.colorScheme.error,
                onClick = {
                    onDismiss()
                    onDelete()
                }
            )
        }
    }
}

@Composable
private fun ActionRowItem(
    icon: ImageVector,
    title: String,
    tint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = tint,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun CreateFolderDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var folderName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إنشاء مجلد جديد", fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = folderName,
                onValueChange = { folderName = it },
                label = { Text("اسم المجلد") },
                placeholder = { Text("مثال: مستندات العمل") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("create_folder_input")
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (folderName.isNotBlank()) {
                        onConfirm(folderName.trim())
                    }
                },
                enabled = folderName.isNotBlank(),
                modifier = Modifier.testTag("create_folder_confirm")
            ) {
                Text("إنشاء")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

@Composable
fun CreateFileDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var fileName by remember { mutableStateOf("") }
    var fileContent by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إنشاء ملف نصي جديد", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = fileName,
                    onValueChange = { fileName = it },
                    label = { Text("اسم الملف (.txt)") },
                    placeholder = { Text("ملاحظة_جديدة") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("create_file_input")
                )
                OutlinedTextField(
                    value = fileContent,
                    onValueChange = { fileContent = it },
                    label = { Text("المحتوى المبدئي (اختياري)") },
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (fileName.isNotBlank()) {
                        onConfirm(fileName.trim(), fileContent)
                    }
                },
                enabled = fileName.isNotBlank(),
                modifier = Modifier.testTag("create_file_confirm")
            ) {
                Text("إنشاء")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

@Composable
fun RenameDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var newName by remember { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إعادة التسمية", fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                label = { Text("الاسم الجديد") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("rename_input")
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (newName.isNotBlank() && newName != currentName) {
                        onConfirm(newName.trim())
                    }
                },
                enabled = newName.isNotBlank() && newName != currentName,
                modifier = Modifier.testTag("rename_confirm")
            ) {
                Text("حفظ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

@Composable
fun DeleteConfirmDialog(
    itemName: String,
    onDismiss: () -> Unit,
    onConfirm: (moveToTrash: Boolean) -> Unit
) {
    var moveToTrash by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تأكيد الحذف", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("هل أنت متأكد من رغبتك في حذف \"$itemName\"؟")
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Checkbox(
                        checked = moveToTrash,
                        onCheckedChange = { moveToTrash = it },
                        modifier = Modifier.testTag("move_to_trash_checkbox")
                    )
                    Text(
                        text = "النقل إلى سلة المحذوفات (يمكن استعادته لاحقاً)",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(moveToTrash) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.testTag("delete_confirm_button")
            ) {
                Text("حذف")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

@Composable
fun FileDetailsDialog(
    item: FileItem,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تفاصيل الملف", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DetailItem(label = "الاسم", value = item.name)
                DetailItem(label = "النوع", value = if (item.isDirectory) "مجلد" else (item.category?.title ?: item.extension.ifEmpty { "غير محدد" }))
                DetailItem(label = "الحجم", value = item.formattedSize)
                DetailItem(label = "المسار", value = item.path)
                DetailItem(label = "تاريخ التعديل", value = item.formattedDate)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("إغلاق")
            }
        }
    )
}

@Composable
private fun DetailItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

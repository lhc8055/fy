package com.so.movie.ui.screen

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.so.movie.rule.Rule
import com.so.movie.ui.theme.*
import com.so.movie.viewmodel.RuleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RuleManagementScreen(
    navController: NavController,
    viewModel: RuleViewModel = viewModel()
) {
    val rules by viewModel.rules.collectAsState()
    val downloadStatus by viewModel.downloadStatus.collectAsState()
    val ruleCatalog by viewModel.ruleCatalog.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showCatalogDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("规则管理", style = MaterialTheme.typography.titleLarge, color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { showImportDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "导入规则")
                    }
                    IconButton(onClick = { viewModel.updateAllRules() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "更新所有规则")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF5F7FA)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 下载状态提示
            if (downloadStatus.isNotEmpty()) {
                Text(
                    text = downloadStatus,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // 统计信息
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "已安装 ${rules.size} 个规则",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Text(
                    text = "${rules.count { it.enabled }} 个启用中",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Primary
                )
            }

            // 操作按钮
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ActionChip(
                    text = "规则仓库",
                    icon = Icons.Default.CloudDownload,
                    onClick = {
                        showCatalogDialog = true
                        viewModel.fetchRuleCatalog()
                    },
                    modifier = Modifier.weight(1f)
                )
                ActionChip(
                    text = "导入规则",
                    icon = Icons.Default.FileUpload,
                    onClick = { showImportDialog = true },
                    modifier = Modifier.weight(1f)
                )
            }

            // 规则列表
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(rules) { rule ->
                    RuleCard(
                        rule = rule,
                        onToggle = { viewModel.toggleRule(rule.name) },
                        onDelete = { viewModel.removeRule(rule.name) }
                    )
                }
            }
        }
    }

    // 规则仓库对话框
    if (showCatalogDialog) {
        CatalogDialog(
            catalog = ruleCatalog,
            onDismiss = { showCatalogDialog = false },
            onInstall = { item ->
                viewModel.downloadRule(item)
            }
        )
    }

    // 导入规则对话框
    if (showImportDialog) {
        ImportRuleDialog(
            onDismiss = { showImportDialog = false },
            onImport = { json ->
                val success = viewModel.importRule(json)
                showImportDialog = false
            }
        )
    }
}

@Composable
private fun ActionChip(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Primary.copy(alpha = 0.1f))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Primary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = Primary
        )
    }
}

@Composable
private fun RuleCard(
    rule: Rule,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 状态指示点
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (rule.enabled) Color(0xFF4CAF50) else Color(0xFFBDBDBD))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = rule.name,
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (rule.builtin) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Primary.copy(alpha = 0.1f))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "内置",
                                style = MaterialTheme.typography.labelSmall,
                                color = Primary,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "v${rule.version} · API ${rule.api} · ${if (rule.searchMode == "api") "API模式" else "XPath模式"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextTertiary,
                    fontSize = 11.sp
                )
                if (rule.baseUrl.isNotEmpty()) {
                    Text(
                        text = rule.baseUrl,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 10.sp
                    )
                }
            }
            // 启用/禁用开关
            Switch(
                checked = rule.enabled,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Primary
                )
            )
        }
    }
}

@Composable
private fun CatalogDialog(
    catalog: List<com.so.movie.rule.RuleCatalogItem>,
    onDismiss: () -> Unit,
    onInstall: (com.so.movie.rule.RuleCatalogItem) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("规则仓库") },
        text = {
            if (catalog.isEmpty()) {
                Text(
                    "正在加载规则列表...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextTertiary
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(catalog) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onInstall(item) }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary
                                )
                                if (item.description.isNotEmpty()) {
                                    Text(
                                        text = item.description,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextTertiary
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "安装",
                                tint = Primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Divider(color = Color(0xFFF0F2F5), thickness = 0.5.dp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("关闭") }
        }
    )
}

@Composable
private fun ImportRuleDialog(
    onDismiss: () -> Unit,
    onImport: (String) -> Unit
) {
    var jsonText by remember { mutableStateOf("") }
    val clipboardManager = LocalClipboardManager.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("导入规则") },
        text = {
            Column {
                Text(
                    "粘贴规则 JSON 或 Base64 字符串：",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = jsonText,
                    onValueChange = { jsonText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    placeholder = { Text("粘贴规则内容...", fontSize = 12.sp) },
                    textStyle = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(4.dp))
                TextButton(
                    onClick = {
                        clipboardManager.getText()?.let { jsonText = it.toString() }
                    }
                ) {
                    Text("从剪贴板粘贴", style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onImport(jsonText) },
                enabled = jsonText.isNotBlank()
            ) { Text("导入") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

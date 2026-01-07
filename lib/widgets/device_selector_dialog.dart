import 'package:flutter/material.dart';

import '../models/device_model.dart';
import '../models/enums.dart';
import '../sm_omron.dart';

/// Callback type for device selection.
typedef OnDeviceSelected = void Function(DeviceModel device);

/// Customizable dialog for selecting Omron devices.
///
/// Shows a list of supported Omron devices grouped by category.
/// Can be displayed as a dialog or bottom sheet.
///
/// Example usage:
/// ```dart
/// final device = await OmronDeviceSelectorDialog.show(
///   context,
///   categoryFilter: DeviceCategory.bloodPressure,
/// );
///   if (device != null && device.deviceIdentifier != null) {
///     final scanned = await smOmron.scanBleDevice(deviceIdentifier: device.deviceIdentifier!);
///   }
/// ```
class OmronDeviceSelectorDialog extends StatefulWidget {
  /// Filter devices by category (null = show all).
  final DeviceCategory? categoryFilter;

  /// Custom title widget for the dialog.
  final Widget? title;

  /// Custom close icon widget.
  final Widget? closeIcon;

  /// Background color for the dialog/sheet.
  final Color? backgroundColor;

  /// Background color for the content (list) area.
  final Color? contentBackgroundColor;

  /// Style for device name text.
  final TextStyle? deviceNameStyle;

  /// Style for device identifier text.
  final TextStyle? deviceIdentifierStyle;

  /// Style for category title text (in grouped view).
  final TextStyle? categoryTitleStyle;

  /// Style for category subtitle text (in grouped view).
  final TextStyle? categorySubtitleStyle;

  /// Color for category icons.
  final Color? categoryIconColor;

  /// Background color for category icons container.
  final Color? categoryIconBackgroundColor;

  /// Custom item builder for full customization.
  final Widget Function(BuildContext, DeviceModel)? itemBuilder;

  /// Callback when device is selected.
  final OnDeviceSelected? onDeviceSelected;

  /// Custom loading widget.
  final Widget? loadingWidget;

  /// Custom empty state widget.
  final Widget? emptyWidget;

  /// Whether to group devices by category.
  final bool groupByCategory;

  const OmronDeviceSelectorDialog({
    super.key,
    this.categoryFilter,
    this.title,
    this.closeIcon,
    this.backgroundColor,
    this.contentBackgroundColor,
    this.deviceNameStyle,
    this.deviceIdentifierStyle,
    this.categoryTitleStyle,
    this.categorySubtitleStyle,
    this.categoryIconColor,
    this.categoryIconBackgroundColor,
    this.itemBuilder,
    this.onDeviceSelected,
    this.loadingWidget,
    this.emptyWidget,
    this.groupByCategory = true,
  });

  /// Show the device selector as a dialog.
  ///
  /// Returns the selected [DeviceModel] or null if cancelled.
  static Future<DeviceModel?> show(
    BuildContext context, {
    DeviceCategory? categoryFilter,
    Widget? title,
    Widget? closeIcon,
    Color? backgroundColor,
    Color? contentBackgroundColor,
    TextStyle? deviceNameStyle,
    TextStyle? deviceIdentifierStyle,
    TextStyle? categoryTitleStyle,
    TextStyle? categorySubtitleStyle,
    Color? categoryIconColor,
    Color? categoryIconBackgroundColor,
    Widget Function(BuildContext, DeviceModel)? itemBuilder,
    bool groupByCategory = true,
  }) {
    return showDialog<DeviceModel>(
      context: context,
      builder: (context) => Dialog(
        clipBehavior: Clip.antiAlias,
        backgroundColor: backgroundColor,
        surfaceTintColor: backgroundColor, // Uniform color for M3
        child: ConstrainedBox(
          constraints: const BoxConstraints(maxHeight: 500, maxWidth: 400),
          child: OmronDeviceSelectorDialog(
            categoryFilter: categoryFilter,
            title: title,
            closeIcon: closeIcon,
            backgroundColor: backgroundColor,
            contentBackgroundColor: contentBackgroundColor,
            deviceNameStyle: deviceNameStyle,
            deviceIdentifierStyle: deviceIdentifierStyle,
            categoryTitleStyle: categoryTitleStyle,
            categorySubtitleStyle: categorySubtitleStyle,
            categoryIconColor: categoryIconColor,
            categoryIconBackgroundColor: categoryIconBackgroundColor,
            itemBuilder: itemBuilder,
            groupByCategory: groupByCategory,
            onDeviceSelected: (device) => Navigator.of(context).pop(device),
          ),
        ),
      ),
    );
  }

  /// Show the device selector as a bottom sheet.
  ///
  /// Returns the selected [DeviceModel] or null if cancelled.
  static Future<DeviceModel?> showAsBottomSheet(
    BuildContext context, {
    DeviceCategory? categoryFilter,
    Widget? title,
    Widget? closeIcon,
    Color? backgroundColor,
    Color? contentBackgroundColor,
    TextStyle? deviceNameStyle,
    TextStyle? deviceIdentifierStyle,
    TextStyle? categoryTitleStyle,
    TextStyle? categorySubtitleStyle,
    Color? categoryIconColor,
    Color? categoryIconBackgroundColor,
    Widget Function(BuildContext, DeviceModel)? itemBuilder,
    bool groupByCategory = true,
  }) {
    return showModalBottomSheet<DeviceModel>(
      context: context,
      isScrollControlled: true,
      backgroundColor: backgroundColor,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(16)),
      ),
      builder: (context) => DraggableScrollableSheet(
        initialChildSize: 0.6,
        minChildSize: 0.3,
        maxChildSize: 0.9,
        expand: false,
        builder: (context, scrollController) => OmronDeviceSelectorDialog(
          categoryFilter: categoryFilter,
          title: title,
          closeIcon: closeIcon,
          backgroundColor: backgroundColor,
          contentBackgroundColor: contentBackgroundColor,
          deviceNameStyle: deviceNameStyle,
          deviceIdentifierStyle: deviceIdentifierStyle,
          categoryTitleStyle: categoryTitleStyle,
          categorySubtitleStyle: categorySubtitleStyle,
          categoryIconColor: categoryIconColor,
          categoryIconBackgroundColor: categoryIconBackgroundColor,
          itemBuilder: itemBuilder,
          groupByCategory: groupByCategory,
          onDeviceSelected: (device) => Navigator.of(context).pop(device),
        ),
      ),
    );
  }

  @override
  State<OmronDeviceSelectorDialog> createState() =>
      _OmronDeviceSelectorDialogState();
}

class _OmronDeviceSelectorDialogState extends State<OmronDeviceSelectorDialog> {
  final SMOmron _smOmron = SMOmron();
  List<DeviceModel>? _devices;
  bool _isLoading = true;
  String? _error;

  @override
  void initState() {
    super.initState();
    _loadDevices();
  }

  Future<void> _loadDevices() async {
    try {
      final devices = await _smOmron.getSupportedDevices(
        category: widget.categoryFilter,
      );
      setState(() {
        _devices = devices;
        _isLoading = false;
      });
    } catch (e) {
      if (mounted) {
        setState(() {
          _error = e.toString();
          _isLoading = false;
        });
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Container(
      color: widget.backgroundColor,
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          _buildHeader(),
          const Divider(height: 1),
          Flexible(child: _buildContent()),
        ],
      ),
    );
  }

  Widget _buildHeader() {
    return Padding(
      padding: const EdgeInsets.all(16),
      child: Row(
        children: [
          Expanded(
            child: widget.title ??
                Text(
                  'Select Omron Device',
                  style: Theme.of(context).textTheme.titleLarge,
                ),
          ),
          IconButton(
            icon: widget.closeIcon ?? const Icon(Icons.close),
            onPressed: () => Navigator.of(context).pop(),
          ),
        ],
      ),
    );
  }

  Widget _buildContent() {
    if (_isLoading) {
      return widget.loadingWidget ??
          const Center(child: CircularProgressIndicator());
    }

    if (_error != null) {
      return Center(
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              const Icon(Icons.error_outline, size: 48, color: Colors.red),
              const SizedBox(height: 16),
              Text('Error loading devices: $_error'),
              const SizedBox(height: 16),
              ElevatedButton(
                onPressed: () {
                  setState(() {
                    _isLoading = true;
                    _error = null;
                  });
                  _loadDevices();
                },
                child: const Text('Retry'),
              ),
            ],
          ),
        ),
      );
    }

    if (_devices == null || _devices!.isEmpty) {
      return widget.emptyWidget ??
          const Center(
            child: Padding(
              padding: EdgeInsets.all(16),
              child: Text('No devices available'),
            ),
          );
    }

    return Container(
      color: widget.contentBackgroundColor,
      child: (widget.groupByCategory && widget.categoryFilter == null)
          ? _buildGroupedList()
          : _buildFlatList(),
    );
  }

  Widget _buildFlatList() {
    return ListView.builder(
      itemCount: _devices!.length,
      itemBuilder: (context, index) => _buildDeviceItem(_devices![index]),
    );
  }

  Widget _buildGroupedList() {
    // Group devices by category
    final grouped = <DeviceCategory, List<DeviceModel>>{};
    for (var device in _devices!) {
      final category =
          DeviceCategory.fromValue(int.tryParse(device.category) ?? 1);
      grouped.putIfAbsent(category, () => []).add(device);
    }

    // Sort categories logic if needed (e.g. BP first)
    final sortedKeys = grouped.keys.toList()
      ..sort((a, b) => a.value.compareTo(b.value));

    return ListView.builder(
      padding: const EdgeInsets.all(8),
      itemCount: sortedKeys.length,
      itemBuilder: (context, index) {
        final category = sortedKeys[index];
        final devices = grouped[category]!;

        return Card(
          margin: const EdgeInsets.only(bottom: 12),
          elevation: 0,
          color: widget.contentBackgroundColor ??
              Theme.of(context).colorScheme.surfaceContainerLow,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(12),
            side: BorderSide(
              color: Theme.of(context).colorScheme.outlineVariant,
            ),
          ),
          clipBehavior: Clip.antiAlias,
          child: Theme(
            // Remove divider line when expanded
            data: Theme.of(context).copyWith(dividerColor: Colors.transparent),
            child: ExpansionTile(
              shape: const Border(), // Remove default border
              collapsedShape: const Border(),
              backgroundColor: widget.contentBackgroundColor ??
                  Theme.of(context).colorScheme.surfaceContainerLow,
              collapsedBackgroundColor: widget.contentBackgroundColor ??
                  Theme.of(context).colorScheme.surface,
              leading: Container(
                padding: const EdgeInsets.all(8),
                decoration: BoxDecoration(
                  color: widget.categoryIconBackgroundColor ??
                      _getCategoryColor(category).withAlpha(30),
                  borderRadius: BorderRadius.circular(8),
                ),
                child: Icon(
                  _getCategoryIcon(category),
                  color:
                      widget.categoryIconColor ?? _getCategoryColor(category),
                  size: 24,
                ),
              ),
              title: Text(
                category.displayName,
                style: widget.categoryTitleStyle ??
                    const TextStyle(
                      fontWeight: FontWeight.bold,
                      fontSize: 16,
                    ),
              ),
              subtitle: Text(
                '${devices.length} devices',
                style: widget.categorySubtitleStyle ??
                    TextStyle(
                      color: Theme.of(context).colorScheme.onSurfaceVariant,
                    ),
              ),
              initiallyExpanded: index == 0, // Expand first item by default
              children: devices.map((d) => _buildDeviceItem(d)).toList(),
            ),
          ),
        );
      },
    );
  }

  Widget _buildDeviceItem(DeviceModel device) {
    if (widget.itemBuilder != null) {
      return InkWell(
        onTap: () => _selectDevice(device),
        child: widget.itemBuilder!(context, device),
      );
    }

    return Column(
      children: [
        const Divider(height: 1, indent: 16, endIndent: 16),
        ListTile(
          contentPadding:
              const EdgeInsets.symmetric(horizontal: 16, vertical: 4),
          leading: Container(
            width: 40,
            height: 40,
            alignment: Alignment.center,
            decoration: BoxDecoration(
              color: Theme.of(context).colorScheme.surfaceContainerHighest,
              borderRadius: BorderRadius.circular(8),
            ),
            // Try to show image if available, else icon
            child: _getDeviceIcon(device),
          ),
          title: Text(
            device.modelDisplayName ?? device.modelName ?? 'Unknown',
            style: widget.deviceNameStyle ??
                const TextStyle(fontWeight: FontWeight.w500),
          ),
          subtitle: Text(
            device.identifier ?? '',
            style: widget.deviceIdentifierStyle ??
                TextStyle(
                  color: Theme.of(context).colorScheme.outline,
                  fontSize: 12,
                ),
          ),
          trailing: Icon(
            Icons.chevron_right,
            size: 20,
            color: Theme.of(context).colorScheme.outline,
          ),
          onTap: () => _selectDevice(device),
        ),
      ],
    );
  }

  Widget _getDeviceIcon(DeviceModel device) {
    // 1. Try to use specific thumbnail if available
    if (device.thumbnail != null && device.thumbnail!.isNotEmpty) {
      return Image.asset(
        'assets/images/${device.thumbnail}.png',
        package: 'sm_omron',
        fit: BoxFit.contain,
        errorBuilder: (context, error, stackTrace) {
          // If specific thumbnail fails, try category image
          return _getCategoryThumbnail(device);
        },
      );
    }

    // 2. Fallback to category image
    return _getCategoryThumbnail(device);
  }

  Widget _getCategoryThumbnail(DeviceModel device) {
    final category =
        DeviceCategory.fromValue(int.tryParse(device.category) ?? 1);

    return Image.asset(
      category.imagePath,
      package: 'sm_omron',
      fit: BoxFit.contain,
      errorBuilder: (context, error, stackTrace) {
        // 3. Final fallback to icon
        return Icon(
          _getCategoryIcon(category),
          size: 20,
          color: Theme.of(context).colorScheme.onSurfaceVariant,
        );
      },
    );
  }

  Color _getCategoryColor(DeviceCategory category) {
    switch (category) {
      case DeviceCategory.bloodPressure:
        return Colors.red;
      case DeviceCategory.weight:
        return Colors.teal;
      case DeviceCategory.activity:
        return Colors.orange;
      case DeviceCategory.pulseOximeter:
        return Colors.blue;
      case DeviceCategory.temperature:
        return Colors.purple;
      case DeviceCategory.wheeze:
        return Colors.indigo;
    }
  }

  IconData _getCategoryIcon(DeviceCategory category) {
    switch (category) {
      case DeviceCategory.bloodPressure:
        return Icons.favorite_rounded;
      case DeviceCategory.weight:
        return Icons.monitor_weight_rounded;
      case DeviceCategory.activity:
        return Icons.directions_run_rounded;
      case DeviceCategory.pulseOximeter:
        return Icons.water_drop_rounded; // Closest to drop/blood/pulse
      case DeviceCategory.temperature:
        return Icons.thermostat_rounded;
      case DeviceCategory.wheeze:
        return Icons.air_rounded;
    }
  }

  void _selectDevice(DeviceModel device) {
    if (widget.onDeviceSelected != null) {
      widget.onDeviceSelected!(device);
    }
  }
}

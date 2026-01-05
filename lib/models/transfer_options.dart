/// Options for data transfer from Omron devices.
class TransferOptions {
  /// Read all historical data from device.
  ///
  /// When `true`, retrieves all stored readings from the device.
  /// When `false` (default), retrieves only new readings since last sync.
  final bool readHistoricalData;

  /// User IDs to read data for (on multi-user devices).
  ///
  /// Default is `[1]` for single-user scenarios.
  /// Some devices support up to 4 users.
  final List<int> userIds;

  /// Connection timeout in seconds.
  ///
  /// Default is 30 seconds.
  final int timeoutSeconds;

  const TransferOptions({
    this.readHistoricalData = false,
    this.userIds = const [1],
    this.timeoutSeconds = 30,
  });

  /// Create TransferOptions from JSON map
  factory TransferOptions.fromJson(Map<String, dynamic> json) {
    return TransferOptions(
      readHistoricalData: json['readHistoricalData'] as bool? ?? false,
      userIds: (json['userIds'] as List<dynamic>?)?.cast<int>() ?? const [1],
      timeoutSeconds: json['timeoutSeconds'] as int? ?? 30,
    );
  }

  /// Convert to JSON map for native platform
  Map<String, dynamic> toJson() {
    return {
      'readHistoricalData': readHistoricalData,
      'userIds': userIds,
      'timeoutSeconds': timeoutSeconds,
    };
  }

  /// Create a copy with modified values
  TransferOptions copyWith({
    bool? readHistoricalData,
    List<int>? userIds,
    int? timeoutSeconds,
  }) {
    return TransferOptions(
      readHistoricalData: readHistoricalData ?? this.readHistoricalData,
      userIds: userIds ?? this.userIds,
      timeoutSeconds: timeoutSeconds ?? this.timeoutSeconds,
    );
  }

  @override
  String toString() {
    return 'TransferOptions(historical: $readHistoricalData, '
        'users: $userIds, timeout: ${timeoutSeconds}s)';
  }
}

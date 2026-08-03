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
  /// Note: When [singleUserMode] is `true`, this is overridden to `[1, 2, 3, 4]`.
  final List<int> userIds;

  /// Connection timeout in seconds.
  ///
  /// Default is 30 seconds.
  final int timeoutSeconds;

  /// When `true`, fetches data from ALL user slots (1-4) on the device
  /// and normalizes all results to userId=1.
  ///
  /// This is useful for devices like the Omron Viva that automatically
  /// assign measurements to different user slots based on weight matching.
  /// With this flag enabled, all measurements will be attributed to user 1
  /// regardless of which user slot the device assigned.
  ///
  /// Default is `false`.
  final bool singleUserMode;

  const TransferOptions({
    this.readHistoricalData = false,
    this.userIds = const [1],
    this.timeoutSeconds = 30,
    this.singleUserMode = false,
  });

  /// Create TransferOptions from JSON map
  factory TransferOptions.fromJson(Map<String, dynamic> json) {
    return TransferOptions(
      readHistoricalData: json['readHistoricalData'] as bool? ?? false,
      userIds: (json['userIds'] as List<dynamic>?)?.cast<int>() ?? const [1],
      timeoutSeconds: json['timeoutSeconds'] as int? ?? 30,
      singleUserMode: json['singleUserMode'] as bool? ?? false,
    );
  }

  /// Convert to JSON map for native platform
  Map<String, dynamic> toJson() {
    return {
      'readHistoricalData': readHistoricalData,
      'userIds': userIds,
      'timeoutSeconds': timeoutSeconds,
      'singleUserMode': singleUserMode,
    };
  }

  /// Create a copy with modified values
  TransferOptions copyWith({
    bool? readHistoricalData,
    List<int>? userIds,
    int? timeoutSeconds,
    bool? singleUserMode,
  }) {
    return TransferOptions(
      readHistoricalData: readHistoricalData ?? this.readHistoricalData,
      userIds: userIds ?? this.userIds,
      timeoutSeconds: timeoutSeconds ?? this.timeoutSeconds,
      singleUserMode: singleUserMode ?? this.singleUserMode,
    );
  }

  @override
  String toString() {
    return 'TransferOptions(historical: $readHistoricalData, '
        'users: $userIds, timeout: ${timeoutSeconds}s, '
        'singleUserMode: $singleUserMode)';
  }
}

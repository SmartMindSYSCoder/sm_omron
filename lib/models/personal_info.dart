/// Personal information for device settings transfer.
///
/// Some Omron devices (weight scales, activity trackers) require personal
/// settings to calculate BMI, body fat percentage, stride length, etc.
class PersonalInfo {
  /// Height in centimeters
  final double heightCm;

  /// Weight in kilograms (for initial device setup)
  final double weightKg;

  /// Stride length in centimeters (for activity trackers)
  final double strideCm;

  /// Date of birth (for body composition calculations)
  final DateTime dateOfBirth;

  /// Gender
  final Gender gender;

  const PersonalInfo({
    required this.heightCm,
    required this.weightKg,
    required this.strideCm,
    required this.dateOfBirth,
    this.gender = Gender.unspecified,
  });

  /// Create PersonalInfo from JSON map
  factory PersonalInfo.fromJson(Map<String, dynamic> json) {
    return PersonalInfo(
      heightCm: (json['heightCm'] as num).toDouble(),
      weightKg: (json['weightKg'] as num).toDouble(),
      strideCm: (json['strideCm'] as num).toDouble(),
      dateOfBirth: DateTime.parse(json['dateOfBirth'] as String),
      gender: Gender.values.firstWhere(
        (e) => e.name == json['gender'],
        orElse: () => Gender.unspecified,
      ),
    );
  }

  /// Convert to JSON map for native platform
  Map<String, dynamic> toJson() {
    return {
      'heightCm': heightCm,
      'weightKg': weightKg,
      'strideCm': strideCm,
      'dateOfBirth': dateOfBirth.toIso8601String(),
      'gender': gender.name,
      // Format for Omron SDK compatibility
      'birthdayNum': _formatBirthdayNum(),
    };
  }

  /// Format birthday as number (YYYYMMDD) for Omron SDK
  String _formatBirthdayNum() {
    return '${dateOfBirth.year}'
        '${dateOfBirth.month.toString().padLeft(2, '0')}'
        '${dateOfBirth.day.toString().padLeft(2, '0')}';
  }

  /// Calculate age from date of birth
  int get age {
    final now = DateTime.now();
    int age = now.year - dateOfBirth.year;
    if (now.month < dateOfBirth.month ||
        (now.month == dateOfBirth.month && now.day < dateOfBirth.day)) {
      age--;
    }
    return age;
  }

  @override
  String toString() {
    return 'PersonalInfo(height: ${heightCm}cm, weight: ${weightKg}kg, '
        'stride: ${strideCm}cm, age: $age, gender: ${gender.name})';
  }
}

/// Gender for personal settings
enum Gender {
  male,
  female,
  unspecified,
}

package com.example.data.sample

import com.example.data.model.Difficulty
import com.example.data.model.QuestionItem
import com.example.data.model.QuestionType
import com.example.data.model.Subject

object SampleJeePapers {

    fun getSamplePaper2025Jan(): List<QuestionItem> {
        val list = mutableListOf<QuestionItem>()

        // ------------------ PHYSICS (Questions 1 to 25) ------------------
        // Q1: Rotational Mechanics (MCQ)
        list.add(
            QuestionItem(
                id = "PHY_01",
                questionNumber = 1,
                subject = Subject.PHYSICS,
                section = "Section A",
                type = QuestionType.MCQ,
                questionText = "A solid sphere of mass M and radius R rolls without slipping down an inclined plane of inclination θ. The acceleration of the sphere down the plane is:",
                options = listOf(
                    "(A) g sin θ",
                    "(B) 5/7 g sin θ",
                    "(C) 2/3 g sin θ",
                    "(D) 3/5 g sin θ"
                ),
                correctAnswer = "B",
                chapter = "Rotational Motion",
                concept = "Rolling on Inclined Plane",
                difficulty = Difficulty.EASY,
                solutionText = "For pure rolling on an inclined plane:\na = (g sin θ) / (1 + I / (M R²))\nFor a solid sphere, I = (2/5) M R².\nSo, a = (g sin θ) / (1 + 2/5) = (5/7) g sin θ.\nCorrect Option: (B)",
                idealTimeSeconds = 60,
                youtubeSearchQuery = "JEE Main Physics solid sphere rolling down inclined plane acceleration"
            )
        )

        // Q2: Current Electricity & Drift Velocity (MCQ)
        list.add(
            QuestionItem(
                id = "PHY_02",
                questionNumber = 2,
                subject = Subject.PHYSICS,
                section = "Section A",
                type = QuestionType.MCQ,
                questionText = "A current of 5 A flows through a copper wire of cross-sectional area 2 × 10⁻⁶ m². If the electron density in copper is 8.5 × 10²⁸ m⁻³, the drift velocity of electrons is approximately (e = 1.6 × 10⁻¹⁹ C):",
                options = listOf(
                    "(A) 1.84 × 10⁻⁴ m/s",
                    "(B) 3.68 × 10⁻⁴ m/s",
                    "(C) 0.92 × 10⁻⁴ m/s",
                    "(D) 5.12 × 10⁻⁴ m/s"
                ),
                correctAnswer = "A",
                chapter = "Current Electricity",
                concept = "Drift Velocity & Current Relation",
                difficulty = Difficulty.MEDIUM,
                solutionText = "Using relation I = n e A v_d:\nv_d = I / (n e A) = 5 / (8.5 × 10²⁸ × 1.6 × 10⁻¹⁹ × 2 × 10⁻⁶)\n= 5 / (2.72 × 10⁴) ≈ 1.838 × 10⁻⁴ m/s.\nCorrect Option: (A)",
                idealTimeSeconds = 90,
                youtubeSearchQuery = "JEE Main Physics drift velocity current density calculation copper wire"
            )
        )

        // Q3: Modern Physics & Photoelectric Effect (MCQ)
        list.add(
            QuestionItem(
                id = "PHY_03",
                questionNumber = 3,
                subject = Subject.PHYSICS,
                section = "Section A",
                type = QuestionType.MCQ,
                questionText = "When light of frequency 2ν₀ (where ν₀ is threshold frequency) is incident on a metal plate, the maximum velocity of emitted electrons is v₁. When light of frequency 5ν₀ is incident, maximum velocity is v₂. The ratio v₁ / v₂ is:",
                options = listOf(
                    "(A) 1 : 2",
                    "(B) 1 : 4",
                    "(C) 1 : √2",
                    "(D) 2 : 1"
                ),
                correctAnswer = "A",
                chapter = "Dual Nature of Matter",
                concept = "Einstein's Photoelectric Equation",
                difficulty = Difficulty.EASY,
                solutionText = "From Einstein's photoelectric equation:\n(1/2) m v₁² = h(2ν₀) - hν₀ = hν₀\n(1/2) m v₂² = h(5ν₀) - hν₀ = 4hν₀\nDividing the two equations: (v₁ / v₂)² = 1 / 4 => v₁ / v₂ = 1 / 2.\nCorrect Option: (A)",
                idealTimeSeconds = 60,
                youtubeSearchQuery = "JEE Main Photoelectric effect frequency ratio velocity emitted electrons"
            )
        )

        // Q4: Thermodynamics & Carnot Cycle (MCQ)
        list.add(
            QuestionItem(
                id = "PHY_04",
                questionNumber = 4,
                subject = Subject.PHYSICS,
                section = "Section A",
                type = QuestionType.MCQ,
                questionText = "A Carnot engine has efficiency of 40% when its sink temperature is 300 K. To increase its efficiency to 60%, keeping the source temperature unchanged, the new sink temperature should be:",
                options = listOf(
                    "(A) 200 K",
                    "(B) 250 K",
                    "(C) 150 K",
                    "(D) 180 K"
                ),
                correctAnswer = "A",
                chapter = "Thermodynamics",
                concept = "Carnot Engine Efficiency",
                difficulty = Difficulty.MEDIUM,
                solutionText = "Efficiency η = 1 - (T_sink / T_source)\nCase 1: 0.40 = 1 - (300 / T_source) => 300 / T_source = 0.60 => T_source = 500 K.\nCase 2: 0.60 = 1 - (T'_sink / 500) => T'_sink / 500 = 0.40 => T'_sink = 200 K.\nCorrect Option: (A)",
                idealTimeSeconds = 75,
                youtubeSearchQuery = "JEE Main Carnot engine efficiency sink temperature change"
            )
        )

        // Q5: Electrostatics & Capacitance (MCQ)
        list.add(
            QuestionItem(
                id = "PHY_05",
                questionNumber = 5,
                subject = Subject.PHYSICS,
                section = "Section A",
                type = QuestionType.MCQ,
                questionText = "A parallel plate capacitor with air between the plates has capacitance C₀. If a dielectric slab of dielectric constant K = 4 and thickness t = d/2 (where d is plate separation) is introduced, the new capacitance is:",
                options = listOf(
                    "(A) (8/5) C₀",
                    "(B) (5/8) C₀",
                    "(C) 2 C₀",
                    "(D) (4/3) C₀"
                ),
                correctAnswer = "A",
                chapter = "Electrostatics",
                concept = "Capacitor with Partial Dielectric Slab",
                difficulty = Difficulty.MEDIUM,
                solutionText = "C = ε₀ A / (d - t + t/K) = ε₀ A / (d - d/2 + d/(2×4))\n= ε₀ A / (d/2 + d/8) = ε₀ A / (5d / 8) = (8/5) (ε₀ A / d) = (8/5) C₀.\nCorrect Option: (A)",
                idealTimeSeconds = 80,
                youtubeSearchQuery = "JEE Main Capacitance dielectric slab partial insertion formula"
            )
        )

        // Q6: Electromagnetic Induction & AC (MCQ)
        list.add(
            QuestionItem(
                id = "PHY_06",
                questionNumber = 6,
                subject = Subject.PHYSICS,
                section = "Section A",
                type = QuestionType.MCQ,
                questionText = "In a series LCR circuit with R = 10 Ω, L = 0.1 H and C = 10 μF connected across an AC source of V = 200 sin(1000 t), the power factor of the circuit is:",
                options = listOf(
                    "(A) 1 / √2",
                    "(B) 1.0 (Unity)",
                    "(C) 0.5",
                    "(D) 0.8"
                ),
                correctAnswer = "B",
                chapter = "Alternating Current",
                concept = "LCR Resonance & Power Factor",
                difficulty = Difficulty.MEDIUM,
                solutionText = "ω = 1000 rad/s\nX_L = ω L = 1000 × 0.1 = 100 Ω\nX_C = 1 / (ω C) = 1 / (1000 × 10 × 10⁻⁶) = 100 Ω\nSince X_L = X_C = 100 Ω, the circuit is at resonance! Z = R = 10 Ω.\nPower factor cos φ = R / Z = 1.0.\nCorrect Option: (B)",
                idealTimeSeconds = 70,
                youtubeSearchQuery = "JEE Main Series LCR resonance power factor calculation"
            )
        )

        // Q21: Physics Numerical Section B
        list.add(
            QuestionItem(
                id = "PHY_21",
                questionNumber = 21,
                subject = Subject.PHYSICS,
                section = "Section B",
                type = QuestionType.NUMERICAL,
                questionText = "A projectile is launched from ground level with speed 50 m/s at an angle of 37° above horizontal. The time taken (in seconds) by the projectile to reach maximum height is (take g = 10 m/s², sin 37° = 0.6):",
                correctAnswer = "3",
                chapter = "Kinematics",
                concept = "Time to reach Maximum Height",
                difficulty = Difficulty.EASY,
                solutionText = "u_y = u sin θ = 50 × sin 37° = 50 × 0.6 = 30 m/s.\nAt maximum height, v_y = 0.\nt = u_y / g = 30 / 10 = 3 s.\nCorrect Answer: 3",
                idealTimeSeconds = 45,
                youtubeSearchQuery = "JEE Main Projectile time of maximum height"
            )
        )

        // Q22: Physics Numerical Section B (Magnetic Force)
        list.add(
            QuestionItem(
                id = "PHY_22",
                questionNumber = 22,
                subject = Subject.PHYSICS,
                section = "Section B",
                type = QuestionType.NUMERICAL,
                questionText = "A circular coil of 100 turns and radius 10 cm carries a current of 2 A. The magnetic field at the center of the coil is x × 10⁻⁴ T. Find the value of x (take π = 3.14):",
                correctAnswer = "13",
                chapter = "Magnetic Effects of Current",
                concept = "Magnetic Field at Centre of Circular Coil",
                difficulty = Difficulty.MEDIUM,
                solutionText = "B = (μ₀ N I) / (2 R) = (4π × 10⁻⁷ × 100 × 2) / (2 × 0.1)\n= 4 × 3.14 × 10⁻⁴ = 12.56 × 10⁻⁴ T ≈ 13 × 10⁻⁴ T (nearest integer is 13).\nCorrect Answer: 13",
                idealTimeSeconds = 90,
                youtubeSearchQuery = "JEE Main Magnetic field center of circular coil formula"
            )
        )

        // ------------------ CHEMISTRY (Questions 26 to 50) ------------------
        // Q26: Chemical Kinetics (MCQ)
        list.add(
            QuestionItem(
                id = "CHE_01",
                questionNumber = 26,
                subject = Subject.CHEMISTRY,
                section = "Section A",
                type = QuestionType.MCQ,
                questionText = "For a first order reaction A → Products, the half-life is 20 minutes. The time taken (in minutes) for 75% completion of the reaction is:",
                options = listOf(
                    "(A) 40",
                    "(B) 60",
                    "(C) 30",
                    "(D) 80"
                ),
                correctAnswer = "A",
                chapter = "Chemical Kinetics",
                concept = "First Order Kinetics & Half Life",
                difficulty = Difficulty.EASY,
                solutionText = "For a 1st order reaction, t_75% = 2 × t_1/2.\nGiven t_1/2 = 20 min.\nt_75% = 2 × 20 = 40 minutes.\nCorrect Option: (A)",
                idealTimeSeconds = 40,
                youtubeSearchQuery = "JEE Main Chemical kinetics half life 75 percent completion"
            )
        )

        // Q27: Organic Chemistry - Aldehydes & Ketones (MCQ)
        list.add(
            QuestionItem(
                id = "CHE_02",
                questionNumber = 27,
                subject = Subject.CHEMISTRY,
                section = "Section A",
                type = QuestionType.MCQ,
                questionText = "Which of the following compounds gives positive Iodoform test and also gives Silver Mirror test with Tollens' Reagent?",
                options = listOf(
                    "(A) CH₃CHO (Acetaldehyde)",
                    "(B) CH₃COCH₃ (Acetone)",
                    "(C) HCHO (Formaldehyde)",
                    "(D) CH₃CH₂OH (Ethanol)"
                ),
                correctAnswer = "A",
                chapter = "Aldehydes, Ketones & Carboxylic Acids",
                concept = "Iodoform and Tollens Test",
                difficulty = Difficulty.EASY,
                solutionText = "Acetaldehyde (CH₃CHO) contains the CH₃C=O group (giving positive iodoform test) and is an aldehyde (giving positive Tollens' silver mirror test).\nCorrect Option: (A)",
                idealTimeSeconds = 45,
                youtubeSearchQuery = "JEE Main Acetaldehyde iodoform and tollens reagent test"
            )
        )

        // Q28: Coordination Compounds (MCQ)
        list.add(
            QuestionItem(
                id = "CHE_03",
                questionNumber = 28,
                subject = Subject.CHEMISTRY,
                section = "Section A",
                type = QuestionType.MCQ,
                questionText = "The spin-only magnetic moment of [Fe(CN)₆]³⁻ complex is approximately (in BM):",
                options = listOf(
                    "(A) 1.73 BM",
                    "(B) 5.92 BM",
                    "(C) 4.90 BM",
                    "(D) 2.83 BM"
                ),
                correctAnswer = "A",
                chapter = "Coordination Compounds",
                concept = "Crystal Field Theory & Magnetic Moment",
                difficulty = Difficulty.MEDIUM,
                solutionText = "Fe is in +3 oxidation state (3d⁵). CN⁻ is a strong field ligand, causing pairing:\nt₂g⁵ eg⁰ -> number of unpaired electrons n = 1.\nSpin-only magnetic moment μ = √(n(n+2)) = √(1 × 3) = √3 ≈ 1.73 BM.\nCorrect Option: (A)",
                idealTimeSeconds = 60,
                youtubeSearchQuery = "JEE Main spin only magnetic moment Fe CN 6 3-"
            )
        )

        // Q29: Electrochemistry & Nernst Equation (MCQ)
        list.add(
            QuestionItem(
                id = "CHE_04",
                questionNumber = 29,
                subject = Subject.CHEMISTRY,
                section = "Section A",
                type = QuestionType.MCQ,
                questionText = "For the cell Zn|Zn²⁺(0.01 M) || Cu²⁺(1.0 M)|Cu, given E°(Zn²⁺/Zn) = -0.76 V and E°(Cu²⁺/Cu) = +0.34 V, the EMF of the cell at 298 K is (2.303 RT/F = 0.059 V):",
                options = listOf(
                    "(A) 1.159 V",
                    "(B) 1.100 V",
                    "(C) 1.041 V",
                    "(D) 1.129 V"
                ),
                correctAnswer = "A",
                chapter = "Electrochemistry",
                concept = "Nernst Equation EMF Calculation",
                difficulty = Difficulty.MEDIUM,
                solutionText = "E°_cell = E°_cathode - E°_anode = 0.34 - (-0.76) = 1.10 V.\nReaction: Zn + Cu²⁺ → Zn²⁺ + Cu (n = 2)\nE_cell = E°_cell - (0.059 / 2) log ([Zn²⁺] / [Cu²⁺])\n= 1.10 - 0.0295 log (0.01 / 1) = 1.10 - 0.0295 (-2) = 1.10 + 0.059 = 1.159 V.\nCorrect Option: (A)",
                idealTimeSeconds = 80,
                youtubeSearchQuery = "JEE Main Daniell cell Nernst equation EMF calculation"
            )
        )

        // Q46: Chemistry Numerical Section B
        list.add(
            QuestionItem(
                id = "CHE_21",
                questionNumber = 46,
                subject = Subject.CHEMISTRY,
                section = "Section B",
                type = QuestionType.NUMERICAL,
                questionText = "The total number of lone pairs of electrons in a molecule of Xenon difluoride (XeF₂) is:",
                correctAnswer = "9",
                chapter = "Chemical Bonding & Molecular Structure",
                concept = "VSEPR Theory & Lone Pairs in Noble Gas Fluorides",
                difficulty = Difficulty.MEDIUM,
                solutionText = "In XeF₂:\n- Central Xe has 8 valence electrons. 2 form single bonds with F, leaving 6 non-bonding electrons = 3 lone pairs on Xe.\n- Each Fluorine atom has 3 lone pairs (7 - 1 = 6 non-bonding electrons) × 2 F atoms = 6 lone pairs.\nTotal lone pairs in the molecule = 3 (on Xe) + 6 (on F) = 9.\nCorrect Answer: 9",
                idealTimeSeconds = 60,
                youtubeSearchQuery = "JEE Main XeF2 total number of lone pairs chemical bonding"
            )
        )

        // ------------------ MATHEMATICS (Questions 51 to 75) ------------------
        // Q51: Definite Integration (MCQ)
        list.add(
            QuestionItem(
                id = "MAT_01",
                questionNumber = 51,
                subject = Subject.MATHEMATICS,
                section = "Section A",
                type = QuestionType.MCQ,
                questionText = "The value of the definite integral ∫[0 to π/2] (sin⁴ x) / (sin⁴ x + cos⁴ x) dx is:",
                options = listOf(
                    "(A) π / 4",
                    "(B) π / 2",
                    "(C) π / 8",
                    "(D) 1"
                ),
                correctAnswer = "A",
                chapter = "Definite Integrals",
                concept = "King's Property of Definite Integrals",
                difficulty = Difficulty.EASY,
                solutionText = "Let I = ∫[0 to π/2] sin⁴x / (sin⁴x + cos⁴x) dx ... (1)\nUsing King's property ∫[a to b] f(x)dx = ∫[a to b] f(a+b-x)dx:\nI = ∫[0 to π/2] cos⁴x / (cos⁴x + sin⁴x) dx ... (2)\nAdding (1) and (2):\n2I = ∫[0 to π/2] 1 dx = π/2 => I = π/4.\nCorrect Option: (A)",
                idealTimeSeconds = 45,
                youtubeSearchQuery = "JEE Main King's property definite integration sin4x cos4x"
            )
        )

        // Q52: Matrices and Determinants (MCQ)
        list.add(
            QuestionItem(
                id = "MAT_02",
                questionNumber = 52,
                subject = Subject.MATHEMATICS,
                section = "Section A",
                type = QuestionType.MCQ,
                questionText = "If A is a 3 × 3 matrix such that |A| = 4, then the value of |adj(2A)| is:",
                options = listOf(
                    "(A) 1024",
                    "(B) 256",
                    "(C) 512",
                    "(D) 64"
                ),
                correctAnswer = "A",
                chapter = "Matrices & Determinants",
                concept = "Properties of Adjoint and Determinant",
                difficulty = Difficulty.MEDIUM,
                solutionText = "For an n × n matrix:\n|2A| = 2ⁿ |A| = 2³ × 4 = 8 × 4 = 32.\n|adj(B)| = |B|^(n - 1) for a 3 × 3 matrix, so |adj(2A)| = |2A|².\n|adj(2A)| = (32)² = 1024.\nCorrect Option: (A)",
                idealTimeSeconds = 60,
                youtubeSearchQuery = "JEE Main determinant of adjoint scalar multiple matrix"
            )
        )

        // Q53: Vectors and 3D Geometry (MCQ)
        list.add(
            QuestionItem(
                id = "MAT_03",
                questionNumber = 53,
                subject = Subject.MATHEMATICS,
                section = "Section A",
                type = QuestionType.MCQ,
                questionText = "If vectors a = 2î + ĵ + k̂ and b = î - 2ĵ + 2k̂, the projection of vector a on vector b is:",
                options = listOf(
                    "(A) 2 / 3",
                    "(B) 4 / 3",
                    "(C) 1 / 3",
                    "(D) 2"
                ),
                correctAnswer = "A",
                chapter = "Vector Algebra",
                concept = "Projection of a Vector",
                difficulty = Difficulty.EASY,
                solutionText = "Projection of a on b = (a · b) / |b|\na · b = (2)(1) + (1)(-2) + (1)(2) = 2 - 2 + 2 = 2.\n|b| = √(1² + (-2)² + 2²) = √(1 + 4 + 4) = √9 = 3.\nProjection = 2 / 3.\nCorrect Option: (A)",
                idealTimeSeconds = 45,
                youtubeSearchQuery = "JEE Main projection of vector dot product formula"
            )
        )

        // Q54: Quadratic Equations & Complex Numbers (MCQ)
        list.add(
            QuestionItem(
                id = "MAT_04",
                questionNumber = 54,
                subject = Subject.MATHEMATICS,
                section = "Section A",
                type = QuestionType.MCQ,
                questionText = "If α and β are the roots of the equation x² - 6x + 2 = 0, then the value of (α³ + β³) is:",
                options = listOf(
                    "(A) 180",
                    "(B) 196",
                    "(C) 216",
                    "(D) 162"
                ),
                correctAnswer = "A",
                chapter = "Quadratic Equations",
                concept = "Symmetric Functions of Roots",
                difficulty = Difficulty.EASY,
                solutionText = "Sum of roots α + β = 6, Product of roots αβ = 2.\nα³ + β³ = (α + β)³ - 3αβ(α + β) = (6)³ - 3(2)(6) = 216 - 36 = 180.\nCorrect Option: (A)",
                idealTimeSeconds = 45,
                youtubeSearchQuery = "JEE Main alpha cube plus beta cube sum of roots quadratic"
            )
        )

        // Q71: Mathematics Numerical Section B
        list.add(
            QuestionItem(
                id = "MAT_21",
                questionNumber = 71,
                subject = Subject.MATHEMATICS,
                section = "Section B",
                type = QuestionType.NUMERICAL,
                questionText = "Let f(x) = x³ - 3x² + 6x + 7. The number of real roots of the equation f'(x) = 0 is:",
                correctAnswer = "0",
                chapter = "Application of Derivatives",
                concept = "Roots of Quadratic Derivative & Monotonicity",
                difficulty = Difficulty.EASY,
                solutionText = "f'(x) = 3x² - 6x + 6 = 3(x² - 2x + 2).\nDiscriminant D of x² - 2x + 2 = (-2)² - 4(1)(2) = 4 - 8 = -4 < 0.\nSince D < 0, f'(x) > 0 for all real x and has NO real roots (0 real roots).\nCorrect Answer: 0",
                idealTimeSeconds = 45,
                youtubeSearchQuery = "JEE Main number of real roots derivative discriminant"
            )
        )

        // Q72: Mathematics Numerical Section B (Permutations & Combinations)
        list.add(
            QuestionItem(
                id = "MAT_22",
                questionNumber = 72,
                subject = Subject.MATHEMATICS,
                section = "Section B",
                type = QuestionType.NUMERICAL,
                questionText = "The number of 4-digit numbers strictly greater than 4000 that can be formed using the digits 2, 3, 4, 5, 6 without repetition is:",
                correctAnswer = "72",
                chapter = "Permutations & Combinations",
                concept = "Forming Numbers with Constraints",
                difficulty = Difficulty.MEDIUM,
                solutionText = "Digits available: {2, 3, 4, 5, 6} (5 distinct digits).\nFor a 4-digit number > 4000, the thousands place can be filled by 4, 5, or 6 (3 choices).\nThe remaining 3 places can be filled from the remaining 4 digits in 4P3 = 4 × 3 × 2 = 24 ways.\nTotal numbers = 3 × 24 = 72.\nCorrect Answer: 72",
                idealTimeSeconds = 60,
                youtubeSearchQuery = "JEE Main permutations combinations 4 digit numbers greater than 4000"
            )
        )

        return list
    }

    fun getSamplePaper2024Shift2(): List<QuestionItem> {
        val list = mutableListOf<QuestionItem>()

        // Physics 1
        list.add(
            QuestionItem(
                id = "P24_01",
                questionNumber = 1,
                subject = Subject.PHYSICS,
                section = "Section A",
                type = QuestionType.MCQ,
                questionText = "The de Broglie wavelength of an electron accelerated through a potential difference of 100 V is approximately (h = 6.63 × 10⁻³⁴ J·s, m_e = 9.1 × 10⁻³¹ kg):",
                options = listOf(
                    "(A) 1.227 Å",
                    "(B) 0.123 Å",
                    "(C) 12.27 Å",
                    "(D) 2.454 Å"
                ),
                correctAnswer = "A",
                chapter = "Dual Nature of Radiation",
                concept = "de Broglie Wavelength of Electron",
                difficulty = Difficulty.EASY,
                solutionText = "λ = 12.27 / √V Å = 12.27 / √100 = 12.27 / 10 = 1.227 Å.\nCorrect Option: (A)",
                idealTimeSeconds = 30,
                youtubeSearchQuery = "JEE Main de Broglie wavelength electron accelerated potential V"
            )
        )

        // Chemistry 1
        list.add(
            QuestionItem(
                id = "C24_01",
                questionNumber = 26,
                subject = Subject.CHEMISTRY,
                section = "Section A",
                type = QuestionType.MCQ,
                questionText = "Which of the following orders is CORRECT for the boiling points of group 16 hydrides?",
                options = listOf(
                    "(A) H₂O > H₂Te > H₂Se > H₂S",
                    "(B) H₂Te > H₂Se > H₂S > H₂O",
                    "(C) H₂O > H₂S > H₂Se > H₂Te",
                    "(D) H₂S > H₂Se > H₂Te > H₂O"
                ),
                correctAnswer = "A",
                chapter = "p-Block Elements",
                concept = "Hydrogen Bonding & Van der Waals Forces",
                difficulty = Difficulty.EASY,
                solutionText = "H₂O has abnormally high boiling point due to extensive intermolecular hydrogen bonding. For H₂S, H₂Se, H₂Te, boiling point increases with molar mass (van der Waals force): H₂Te > H₂Se > H₂S.\nHence: H₂O > H₂Te > H₂Se > H₂S.\nCorrect Option: (A)",
                idealTimeSeconds = 45,
                youtubeSearchQuery = "JEE Main boiling point group 16 hydrides p block"
            )
        )

        // Maths 1
        list.add(
            QuestionItem(
                id = "M24_01",
                questionNumber = 51,
                subject = Subject.MATHEMATICS,
                section = "Section A",
                type = QuestionType.MCQ,
                questionText = "The area (in sq. units) of the region bounded by the parabola y² = 4x and the line y = 2x is:",
                options = listOf(
                    "(A) 1 / 3",
                    "(B) 2 / 3",
                    "(C) 4 / 3",
                    "(D) 1 / 6"
                ),
                correctAnswer = "A",
                chapter = "Area Under Curves",
                concept = "Area between Parabola and Straight Line",
                difficulty = Difficulty.MEDIUM,
                solutionText = "Intersection points: (2x)² = 4x => 4x² = 4x => x = 0 and x = 1.\nArea = ∫[0 to 1] (2√x - 2x) dx = [2(x^(3/2) / (3/2)) - x²]₀¹ = [4/3 (1) - 1] = 1/3 sq. units.\nShortcut: Area between y² = 4ax and y = mx is 8 a² / (3 m³) = 8(1)² / (3 × 2³) = 8 / 24 = 1/3.\nCorrect Option: (A)",
                idealTimeSeconds = 60,
                youtubeSearchQuery = "JEE Main area bounded by parabola y2=4ax and line y=mx"
            )
        )

        // Maths Numerical
        list.add(
            QuestionItem(
                id = "M24_21",
                questionNumber = 71,
                subject = Subject.MATHEMATICS,
                section = "Section B",
                type = QuestionType.NUMERICAL,
                questionText = "The maximum value of 3 sin θ + 4 cos θ + 5 is:",
                correctAnswer = "10",
                chapter = "Trigonometric Functions",
                concept = "Range of a sin θ + b cos θ",
                difficulty = Difficulty.EASY,
                solutionText = "Max value of a sin θ + b cos θ is √(a² + b²).\nHere √(3² + 4²) = √(9 + 16) = √25 = 5.\nMax value = 5 + 5 = 10.\nCorrect Answer: 10",
                idealTimeSeconds = 30,
                youtubeSearchQuery = "JEE Main maximum value a sin theta plus b cos theta"
            )
        )

        return list
    }

    fun getQuizrrPartTest02(): List<QuestionItem> {
        return QuizrrPartTest02Data.getQuestions()
    }
}

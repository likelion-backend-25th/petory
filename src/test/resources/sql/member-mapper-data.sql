INSERT INTO member (
    email, password, nickname, species, sex, birth_date,
    profile_image, role
) VALUES (
    'mapper-test@petory.com',
    '{bcrypt}encoded-password',
    '멍치테스트',
    '개',
    'M',
    '2020-05-01',
    'https://example.com/profile/mapper-test.png',
    'ROLE_USER'
);

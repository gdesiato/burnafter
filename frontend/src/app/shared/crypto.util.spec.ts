import { newMessageKey, keyFromFragment, encrypt, decrypt, isPasswordFragment } from './crypto.util';

describe('crypto.util', () => {
  it('round-trips with a link key', async () => {
    const { key, fragment } = await newMessageKey('');
    const { ciphertext, iv } = await encrypt('hello', key);

    expect(isPasswordFragment(fragment)).toBeFalse();
    expect(await decrypt(ciphertext, iv, await keyFromFragment(fragment, ''))).toBe('hello');
  });

  it('round-trips with a password, and the fragment holds only the salt', async () => {
    const { key, fragment } = await newMessageKey('s3cret');
    const { ciphertext, iv } = await encrypt('hello', key);

    expect(isPasswordFragment(fragment)).toBeTrue();
    expect(await decrypt(ciphertext, iv, await keyFromFragment(fragment, 's3cret'))).toBe('hello');
  });

  it('fails with the wrong password', async () => {
    const { key, fragment } = await newMessageKey('s3cret');
    const { ciphertext, iv } = await encrypt('hello', key);

    await expectAsync(decrypt(ciphertext, iv, await keyFromFragment(fragment, 'wrong'))).toBeRejected();
  });

  it('fails if the ciphertext was tampered with (GCM authentication)', async () => {
    const { key } = await newMessageKey('');
    const { ciphertext, iv } = await encrypt('hello', key);
    const tampered = (ciphertext[0] === 'A' ? 'B' : 'A') + ciphertext.slice(1);

    await expectAsync(decrypt(tampered, iv, key)).toBeRejected();
  });

  it('uses a fresh IV every time', async () => {
    const { key } = await newMessageKey('');
    const a = await encrypt('same text', key);
    const b = await encrypt('same text', key);

    expect(a.iv).not.toBe(b.iv);
    expect(a.ciphertext).not.toBe(b.ciphertext);
  });
});

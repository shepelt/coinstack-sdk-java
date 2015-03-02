package io.cloudwallet.coinstack;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.ScriptException;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.crypto.DeterministicKey;
import org.bitcoinj.crypto.TransactionSignature;
import org.bitcoinj.script.Script;
import org.bitcoinj.signers.LocalTransactionSigner;
import org.bitcoinj.signers.StatelessTransactionSigner;
import org.bitcoinj.wallet.KeyBag;
import org.bitcoinj.wallet.RedeemData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoinStackSigner extends StatelessTransactionSigner {
	private static final Logger log = LoggerFactory
			.getLogger(LocalTransactionSigner.class);

	public boolean isReady() {
		return true;
	}
	
	public ECKey signingKey;

	public boolean signInputs(ProposedTransaction propTx, KeyBag keyBag) {
		Transaction tx = propTx.partialTx;
		int numInputs = tx.getInputs().size();
		for (int i = 0; i < numInputs; i++) {
			TransactionInput txIn = tx.getInput(i);
			try {
				// We assume if its already signed, its hopefully got a SIGHASH
				// type that will not invalidate when
				// we sign missing pieces (to check this would require either
				// assuming any signatures are signing
				// standard output types or a way to get processed signatures
				// out of script execution)
				txIn.getScriptSig().correctlySpends(tx, i,
						new Script(txIn.getScriptBytes()));
				log.warn(
						"Input {} already correctly spends output, assuming SIGHASH type used will be safe and skipping signing.",
						i);
				continue;
			} catch (ScriptException e) {
				// Expected.
			}

//			RedeemData redeemData = txIn.getConnectedRedeemData(keyBag);
			RedeemData redeemData = RedeemData.of(signingKey, txIn.getScriptSig());

//			Script scriptPubKey = txIn.getConnectedOutput().getScriptPubKey();
			Script scriptPubKey = new Script(txIn.getScriptBytes());

			// For P2SH inputs we need to share derivation path of the signing
			// key with other signers, so that they
			// use correct key to calculate their signatures.
			// Married keys all have the same derivation path, so we can safely
			// just take first one here.
			ECKey pubKey = redeemData.keys.get(0);
			if (pubKey instanceof DeterministicKey)
				propTx.keyPaths.put(scriptPubKey,
						(((DeterministicKey) pubKey).getPath()));

			ECKey key;
			// locate private key in redeem data. For pay-to-address and
			// pay-to-key inputs RedeemData will always contain
			// only one key (with private bytes). For P2SH inputs RedeemData
			// will contain multiple keys, one of which MAY
			// have private bytes
			if (redeemData == null || (key = redeemData.getFullKey()) == null) {
				log.warn("No local key found for input {}", i);
				continue;
			}

			Script inputScript = new Script(txIn.getScriptBytes());
			// script here would be either a standard CHECKSIG program for
			// pay-to-address or pay-to-pubkey inputs or
			// a CHECKMULTISIG program for P2SH inputs
			byte[] script = redeemData.redeemScript.getProgram();
			try {
				TransactionSignature signature = tx.calculateSignature(i, key,
						script, Transaction.SigHash.ALL, false);

				// at this point we have incomplete inputScript with OP_0 in
				// place of one or more signatures. We already
				// have calculated the signature using the local key and now
				// need to insert it in the correct place
				// within inputScript. For pay-to-address and pay-to-key script
				// there is only one signature and it always
				// goes first in an inputScript (sigIndex = 0). In P2SH input
				// scripts we need to figure out our relative
				// position relative to other signers. Since we don't have that
				// information at this point, and since
				// we always run first, we have to depend on the other signers
				// rearranging the signatures as needed.
				// Therefore, always place as first signature.
				int sigIndex = 0;
				inputScript = scriptPubKey.getScriptSigWithSignature(
						inputScript, signature.encodeToBitcoin(), sigIndex);
				txIn.setScriptSig(inputScript);

			} catch (ECKey.KeyIsEncryptedException e) {
				throw e;
			} catch (ECKey.MissingPrivateKeyException e) {
				log.warn("No private key in keypair for input {}", i);
			}

		}
		return true;
	}

}